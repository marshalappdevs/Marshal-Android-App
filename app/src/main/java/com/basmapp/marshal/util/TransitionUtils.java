package com.basmapp.marshal.util;

import android.transition.Transition;

/**
 * Utility methods for working with transitions
 */
public class TransitionUtils {

    private TransitionUtils() {
    }

    public static class TransitionListenerAdapter implements Transition.TransitionListener {

        @Override
        public void onTransitionStart(Transition transition) {
        }

        @Override
        public void onTransitionEnd(Transition transition) {
        }

        @Override
        public void onTransitionCancel(Transition transition) {
        }

        @Override
        public void onTransitionPause(Transition transition) {
        }

        @Override
        public void onTransitionResume(Transition transition) {
        }
    }
}