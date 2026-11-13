package org.edgepose.core

/** Combines independent primary and secondary model evidence without mixing sides. */
object KneeEvidenceMerger {
    fun merge(primary: KneeAlignmentEvidence?, secondary: KneeAlignmentEvidence?): KneeAlignmentEvidence? {
        if (primary == null && secondary == null) return null
        return KneeAlignmentEvidence(
            left = mergeSide(primary?.left, secondary?.left),
            right = mergeSide(primary?.right, secondary?.right),
        )
    }

    private fun mergeSide(primary: KneeSideEvidence?, secondary: KneeSideEvidence?): KneeSideEvidence? {
        if (primary == null && secondary == null) return null
        return KneeSideEvidence(
            detected = primary?.detected == true || secondary?.detected == true,
            strongDetected = primary?.strongDetected == true || secondary?.strongDetected == true,
        )
    }
}
