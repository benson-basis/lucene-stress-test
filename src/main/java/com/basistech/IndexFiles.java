/******************************************************************************
 ** This data and information is proprietary to, and a valuable trade secret
 ** of, Basis Technology Corp.  It is given in confidence by Basis Technology
 ** and may only be used as permitted under the license agreement under which
 ** it has been distributed, and in no other way.
 **
 ** Copyright (c) 2013 Basis Technology Corporation All rights reserved.
 **
 ** The technical data and information provided herein are provided with
 ** `limited rights', and the computer software provided herein is provided
 ** with `restricted rights' as those terms are defined in DAR and ASPR
 ** 7-104.9(a).
 ******************************************************************************/

package com.basistech;

import com.basistech.rlp.solr.RLPTokenizerFactory;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilterFactory;
import org.apache.lucene.analysis.icu.ICUFoldingFilterFactory;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilterFactory;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.solr.analysis.ReversedWildcardFilterFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * Created by benson on 12/31/13.
 */
public final class IndexFiles {
    private Analyzer analyzer;
    private String btroot;
    private String inputs;

    private IndexFiles(String btroot, String inputs) {
        this.btroot = btroot;
        this.inputs = inputs;

    }

    /*
          <fieldType name="keyword_ara" class="solr.TextField" positionIncrementGap="100" sortMissingFirst="true">
         <!-- Arabic language field type (depends on Rosette Linguistics Platform) -->
         <analyzer type="index">
            <charFilter class="solr.HTMLStripCharFilterFactory"/>
            <filter class="solr.ICUFoldingFilterFactory"/>
            <tokenizer class="com.basistech.rlp.solr.RLPTokenizerFactory" rlpContext="/data/rlp/lucene/conf/rlp-context-ara.xml" lang="ara" postLemma="true" postCompoundComponents="true" postPartOfSpeech="false"/>
            <filter class="solr.ReversedWildcardFilterFactory"/>
            <filter class="org.apache.solr.analysis.RemoveDuplicatesTokenFilterFactory"/>
         </analyzer>
         <analyzer type="query">
            <charFilter class="solr.HTMLStripCharFilterFactory"/>
            <filter class="solr.ICUFoldingFilterFactory"/>
            <tokenizer class="com.basistech.rlp.solr.RLPTokenizerFactory" rlpContext="/data/rlp/lucene/conf/rlp-context-ara.xml" lang="ara" postAltLemmas="true" postPartOfSpeech="false"/>
            <filter class="org.apache.solr.analysis.RemoveDuplicatesTokenFilterFactory"/>
         </analyzer>
      </fieldType>

     */

    public static void main(String[] args) throws IOException {
        String btroot = args[0];
        String inputs = args[1];

        IndexFiles indexFiles = new IndexFiles(btroot, inputs);
        indexFiles.go();
    }

    private void go() throws IOException {
        System.setProperty("bt.root", btroot);
        analyzer = setupAnalyzer(true);
        iterateOverFiles(new File(inputs));
    }

    static Analyzer setupAnalyzer(final boolean filters) {
        Map<String, String> tokenizerFactoryArgs = Maps.newHashMap();
        tokenizerFactoryArgs.put("rlpContext", "rlp-context-ara.xml");
        tokenizerFactoryArgs.put("lang", "ara");
        tokenizerFactoryArgs.put("postLemma", "true");
        tokenizerFactoryArgs.put("postCompoundComponents", "true");
        tokenizerFactoryArgs.put("postPartOfSpeech", "true");


        final RLPTokenizerFactory tokenizerFactory = new RLPTokenizerFactory(tokenizerFactoryArgs);
        Map<String, String> emptyOptions = Maps.newHashMap();
        final HTMLStripCharFilterFactory charFilterFactory = new HTMLStripCharFilterFactory(emptyOptions);
        final ICUFoldingFilterFactory foldingFilterFactory = new ICUFoldingFilterFactory(emptyOptions);
        final ReversedWildcardFilterFactory reversedWildcardFilterFactory = new ReversedWildcardFilterFactory(emptyOptions);
        final RemoveDuplicatesTokenFilterFactory removeDuplicatesTokenFilterFactory = new RemoveDuplicatesTokenFilterFactory(emptyOptions);

        return new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName,
                                                             Reader reader) {
                final Tokenizer source = tokenizerFactory.create(reader);
                if (filters) {
                    TokenStream filter = foldingFilterFactory.create(source);
                    filter = reversedWildcardFilterFactory.create(filter);
                    filter = removeDuplicatesTokenFilterFactory.create(filter);
                    return new TokenStreamComponents(source, filter);
                } else {
                    return new TokenStreamComponents(source);
                }
            }

            @Override
            protected Reader initReader(String fieldName, Reader reader) {
                if (filters) {
                    return charFilterFactory.create(reader);
                } else {
                    return reader;
                }
            }
        };
    }


    private void iterateOverFiles(File directory) throws IOException {
        File[] textFiles = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".txt");
            }
        });

        for (File dataFile : textFiles) {
            Reader dataReader = null;
            try {
                dataReader = Files.newReader(dataFile, Charsets.UTF_8);
                TokenStream tokenStream = analyzer.tokenStream("full_text", dataReader);
                tokenStream.reset();
                OffsetAttribute offsets = tokenStream.getAttribute(OffsetAttribute.class);

                while (tokenStream.incrementToken()) {
                    System.out.println(offsets.startOffset());
                }
            } finally {
                IOUtils.closeQuietly(dataReader);
            }
        }

    }
}
