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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.LuceneTestCase;
import org.junit.Test;

import static org.apache.lucene.analysis.BaseTokenStreamTestCase.checkRandomData;

/**
 * Created by benson on 12/31/13.
 */
public class RandomStress extends LuceneTestCase {
    @Test
    public void testRandom() throws Exception {
        Analyzer analyzer = IndexFiles.setupAnalyzer(false);
        checkRandomData(random(), analyzer, 1000000, 1000);
    }
}
