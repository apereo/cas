package org.apereo.cas.lucene;

import org.apereo.cas.util.function.FunctionUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * This is {@link LuceneSearchService}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
public class LuceneSearchService {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final File indexDirectory;
    private final List<String> indexFields;
    private final List<String> textFields;

    /**
     * Delete indexes.
     */
    public void deleteIndexes() {
        FunctionUtils.doUnchecked(_ -> FileUtils.deleteDirectory(indexDirectory));
    }

    /**
     * Index.
     *
     * @param input the input
     */
    public void createIndexes(final InputStream input) {
        FunctionUtils.doAndHandle(_ -> {
            val analyzer = new StandardAnalyzer();
            val config = new IndexWriterConfig(analyzer);
            try (val directory = new NIOFSDirectory(indexDirectory.toPath());
                 val indexWriter = new IndexWriter(directory, config)) {
                val rootNode = MAPPER.readTree(input);
                for (val indexField : indexFields) {
                    if (rootNode.has(indexField)) {
                        indexNode(rootNode.get(indexField), indexWriter);
                    }
                }
            }
        });
    }

    /**
     * First search result.
     *
     * @param searchFields the search fields
     * @param searchTerm   the search term
     * @return the list
     * @throws Exception the exception
     */
    public SearchResult first(final String searchFields, final String searchTerm) throws Exception {
        return search(List.of(searchFields.split(",")), searchTerm, 10).getFirst();
    }

    /**
     * Search data by entries.
     *
     * @param searchFields the search entries
     * @param searchTerm   the query str
     * @return the list
     * @throws Exception the exception
     */
    public List<SearchResult> search(final List<String> searchFields,
                                     final String searchTerm, final int count) throws Exception {
        val results = new ArrayList<SearchResult>();
        try (val directory = FSDirectory.open(indexDirectory.toPath());
             val reader = DirectoryReader.open(directory)) {
            val searcher = new IndexSearcher(reader);

            val booleanQuery = new BooleanQuery.Builder();
            for (val searchField : searchFields) {
                val query = new WildcardQuery(new Term(searchField, '*' + searchTerm.toLowerCase(Locale.ENGLISH) + '*'));
                booleanQuery.add(query, BooleanClause.Occur.SHOULD);

                val words = Splitter.on(' ').splitToList(searchTerm.toLowerCase(Locale.ENGLISH));
                val phraseQuery = new PhraseQuery.Builder();
                phraseQuery.setSlop(1);

                for (val word : words) {
                    phraseQuery.add(new Term(searchField, word));
                }
                booleanQuery.add(phraseQuery.build(), BooleanClause.Occur.SHOULD);

                val termQuery = new TermQuery(new Term(searchField, searchTerm.toLowerCase(Locale.ENGLISH)));
                booleanQuery.add(termQuery, BooleanClause.Occur.SHOULD);
            }
            val query = booleanQuery.build();
            val topDocs = searcher.search(query, count, Sort.RELEVANCE, true);

            for (val scoreDoc : topDocs.scoreDocs) {
                val doc = searcher.storedFields().document(scoreDoc.doc);
                val fields = doc.getFields().stream()
                    .map(field -> {
                        val highlight = field.stringValue().replace(searchTerm, "<i><b><u>" + searchTerm + "</i></b></u>");
                        return new ResultEntry(field.name(), highlight);
                    })
                    .toList();
                results.add(new SearchResult(fields));
            }
        }
        return results;
    }

    private void indexNode(final JsonNode rootNode, final IndexWriter indexWriter) throws IOException {
        if (rootNode.isArray()) {
            for (val node : rootNode) {
                indexDocument(indexWriter, node);
            }
        } else {
            indexDocument(indexWriter, rootNode);
        }
    }

    private void indexDocument(final IndexWriter indexWriter, final JsonNode node) throws IOException {
        val document = new Document();
        textFields.forEach(field -> {
            if (node.has(field)) {
                val fieldType = new FieldType(TextField.TYPE_STORED);
                fieldType.setTokenized(false);
                fieldType.setStoreTermVectorOffsets(true);
                fieldType.setStoreTermVectors(true);
                fieldType.setStoreTermVectorPayloads(true);
                fieldType.setStoreTermVectorPositions(true);
                document.add(new Field(field, node.get(field).asString(), fieldType));
            }
        });
        indexWriter.addDocument(document);
    }

    public record ResultEntry(String field, String value) {
    }

    public record SearchResult(List<ResultEntry> entries) {
        /**
         * Gets value.
         *
         * @param name the name
         * @return the value
         */
        @JsonIgnore
        public Optional<String> getValue(final String name) {
            return entries
                .stream()
                .filter(entry -> entry.field().equals(name))
                .findFirst()
                .map(ResultEntry::value);
        }

        /**
         * Gets long.
         *
         * @param name the name
         * @return the long
         */
        @JsonIgnore
        public Optional<Long> getLong(final String name) {
            return getValue(name).map(Long::valueOf);
        }
    }
}
