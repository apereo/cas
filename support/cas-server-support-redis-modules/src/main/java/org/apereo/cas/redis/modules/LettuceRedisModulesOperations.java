package org.apereo.cas.redis.modules;

import com.redis.lettucemod.api.sync.RedisModulesCommands;
import com.redis.lettucemod.search.CreateOptions;
import com.redis.lettucemod.search.Document;
import com.redis.lettucemod.search.Field;
import com.redis.lettucemod.search.SearchResults;
import lombok.RequiredArgsConstructor;
import lombok.val;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This is {@link LettuceRedisModulesOperations}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
public class LettuceRedisModulesOperations implements RedisModulesOperations {
    private final RedisModulesCommands commands;

    @Override
    public void createIndexes(final String indexName, final String prefix,
                              final List<String> fields) {
        val options = CreateOptions.builder()
            .prefix(prefix)
            .maxTextFields(true)
            .build();
        val createIndex = commands.ftList().parallelStream().noneMatch(idx -> indexName.equalsIgnoreCase(idx.toString()));
        if (createIndex) {
            val indexFields = fields.stream().map(field -> Field.text(field).build()).toList();
            commands.ftCreate(indexName, options, indexFields.toArray(new Field[]{}));
        }
    }

    @Override
    public Stream<Map<String, String>> search(final String searchIndexName, final String query) {
        val results = (SearchResults<String, Document>) commands.ftSearch(searchIndexName, query);
        return results
            .parallelStream()
            .map(Document.class::cast)
            .filter(document -> !document.isEmpty())
            .map(LinkedHashMap.class::cast);
    }
}
