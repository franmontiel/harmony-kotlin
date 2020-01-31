package com.harmony.kotlin.android.application.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager


fun Application.initInjection() {

  this.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
    }

    override fun onActivitySaveInstanceState(activity: Activity?, bundle: Bundle?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
      if (activity is com.harmony.kotlin.android.application.vm.VMBaseActivity) {
        dagger.android.AndroidInjection.inject(activity)
        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
              override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                if (f is com.harmony.kotlin.android.application.di.Injectable) {
                  dagger.android.support.AndroidSupportInjection.inject(f)
                }
              }
            }, true)
      }
    }
  })
}