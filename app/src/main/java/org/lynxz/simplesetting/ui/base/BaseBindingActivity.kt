package org.lynxz.simplesetting.ui.base

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding


/**
 * 带databinding页面
 */
abstract class BaseBindingActivity<B : ViewDataBinding> : BaseActivity() {
    protected lateinit var dataBinding: B

    override fun onGetLayoutResSuccessful(layoutRes: Int) {
//        val  containerView = layoutInflater.inflate(layoutRes, null, false)
//        dataBinding = DataBindingUtil.bind(containerView)!!
        dataBinding = DataBindingUtil.setContentView(this, layoutRes)
    }

    companion object {
        private const val TAG = "BaseBindingActivity"
    }
}