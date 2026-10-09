package org.apereo.cas.gradle

import groovy.io.FileType
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

abstract class TestCategoryTagsValueSource implements ValueSource<List<String>, Parameters> {
    interface Parameters extends ValueSourceParameters {
        ListProperty<String> getSourceDirectories()
    }

    @Override
    List<String> obtain() {
        def tags = new TreeSet<String>()
        def tagPattern = ~/^\s*@Tag\("(\w+)"\)\s*$/
        parameters.sourceDirectories.get().each { directory ->
            def sourceDirectory = new File(directory)
            if (sourceDirectory.exists()) {
                sourceDirectory.traverse(type: FileType.FILES, nameFilter: ~/.*Tests\.\w+/) { file ->
                    file.eachLine('UTF-8') { line ->
                        def match = line =~ tagPattern
                        if (match.find()) {
                            tags.add(match.group(1))
                        }
                    }
                }
            }
        }
        Collections.unmodifiableList(new ArrayList<String>(tags))
    }
}
