package org.edgepose.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KneeEvidenceMergerTest {
    @Test fun mergesRiskPerSideAndPreservesCoverage() {
        val primary = KneeAlignmentEvidence(KneeSideEvidence(false, false), null)
        val secondary = KneeAlignmentEvidence(null, KneeSideEvidence(true, true))
        val merged = KneeEvidenceMerger.merge(primary, secondary)!!
        assertEquals(false, merged.left!!.detected)
        assertTrue(merged.right!!.detected)
        assertTrue(merged.right!!.strongDetected)
        assertEquals(true, merged.covered)
    }

    @Test fun noModelEvidenceReturnsNoEvidence() {
        assertEquals(null, KneeEvidenceMerger.merge(null, null))
    }
}
