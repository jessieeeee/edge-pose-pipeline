package org.edgepose.demo

enum class DemoMode(val showSyntheticPose: Boolean, val showReplayMetrics: Boolean) {
    REPLAY(true, true),
    CAMERA(false, false),
}
