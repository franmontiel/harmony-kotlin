package com.mobilejazz.kotlin.core.viewmodel

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.mobilejazz.kotlin.core.di.Injectable
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

abstract class VMBaseActivity : AppCompatActivity(), HasSupportFragmentInjector, () -> Lifecycle, Injectable{

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  @Inject
  lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

  override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector

  inline fun <reified VM : ViewModel> getViewModel(): VM = ViewModelProviders.of(this, viewModelFactory).get(VM::class.java)

  override fun invoke(): Lifecycle = lifecycle

}