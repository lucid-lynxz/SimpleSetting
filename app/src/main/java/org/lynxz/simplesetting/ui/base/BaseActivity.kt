package org.lynxz.simplesetting.ui.base

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.noober.background.BackgroundLibrary
import org.lynxz.simplesetting.R
import org.lynxz.utils.ScreenUtil
import org.lynxz.utils.log.LoggerUtil
import org.lynxz.utils.trans.BaseTransFragment
import org.lynxz.utils.trans.IPermissionCallback
import org.lynxz.utils.trans.PermissionFragment
import org.lynxz.utils.trans.PermissionResultInfo
import org.lynxz.utils.trans.permissionCheckerImpl.IgnoreBatteryOptimCheckerImpl

abstract class BaseActivity : AppCompatActivity(), IPermissionCallback {
    private val permissionFrag by lazy {
        BaseTransFragment.getTransFragment(this, "permission_tag", PermissionFragment().apply {
            registerPermissionChecker(IgnoreBatteryOptimCheckerImpl)
        })
    }

    protected fun requestPermission(permission: String, callback: IPermissionCallback? = this) {
        permissionFrag?.requestPermission(permission, callback)
    }

    protected fun requestPermissions(
        permissions: Array<String>,
        callback: IPermissionCallback? = this
    ) {
        permissionFrag?.requestPermissions(permissions, callback)
    }

    override fun onRequestResult(permission: PermissionResultInfo) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // 竖屏
        super.onCreate(savedInstanceState)
        BackgroundLibrary.inject(this)
        beforeSetContentView()
        val layoutRes = getLayoutRes()

        // 可自定义布局或者通过fragment替换
        if (layoutRes == 0) {
            setContentView(R.layout.activity_base)

            getContentFragment()?.let {
                findViewById<View>(R.id.rl_base_container).visibility = View.VISIBLE
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.rl_base_container, it)
                    .commit()

                intent?.extras?.let { bundle ->
                    if (it is BaseFragment
                        && it.shouldInterceptParentArgumentTrans()
                    ) {
                        return@let
                    }

                    if (bundle.size() > 0) {
                        it.arguments = bundle
                    }
                }
            }
        } else {
            onGetLayoutResSuccessful(layoutRes)
        }
        afterViewCreated()
    }

    /**
     * 获取到布局id(非0) 时回调, 子类可重写用于切换 databinding 等框架
     * */
    open fun onGetLayoutResSuccessful(@LayoutRes layoutRes: Int) {
        setContentView(layoutRes)
    }


    /**
     * 在 android.R.id.content 中追加一个fragment
     */
    open fun showFragment(fragment: BaseFragment?, tag: String? = null) {
        if (fragment == null) {
            return
        }

        val fTag = if (tag.isNullOrBlank()) fragment::class.java.canonicalName else tag
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fragment_in,
                R.anim.fragment_out,
                R.anim.enter_from_bottom_to_top,
                R.anim.exit_from_top_to_bottom
            )
            .add(android.R.id.content, fragment, fTag)
            .addToBackStack(fTag)
            .commit()
    }

    /**
     * 移除指定的fragment
     */
    open fun hideContentFragment(fragment: BaseFragment) {
        supportFragmentManager.beginTransaction().remove(fragment).commit()
    }

    private val mHandler by lazy { Handler() }
    private val delayActionMap by lazy { mutableMapOf<Long, Runnable>() }

    fun doDelay(delayMs: Long, tag: Long = System.currentTimeMillis(), block: () -> Unit) {
        delayActionMap[tag]?.let {
            mHandler.removeCallbacks(it)
        }

        val delayRunnable = Runnable {
            LoggerUtil.d("doDelay action $tag , running.... $isFinishing")
            if (!isFinishing) {
                block()
            }
        }

        delayActionMap[tag] = delayRunnable
        mHandler.postDelayed(delayRunnable, delayMs)
    }

    protected fun cancelDelayAction(tag: Long = Long.MIN_VALUE) {
        LoggerUtil.d("cancelDelayAction $tag")
        if (delayActionMap.isEmpty()) {
            return
        }

        for ((k, v) in delayActionMap) {
            if (tag == Long.MIN_VALUE) {
                mHandler.removeCallbacks(v)
            } else if (k == tag) {
                mHandler.removeCallbacks(v)
                break
            }
        }

        if (tag == Long.MIN_VALUE) {
            delayActionMap.clear()
        } else {
            delayActionMap.remove(tag)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cancelDelayAction()
    }


    @LayoutRes
    abstract fun getLayoutRes(): Int

    /**
     * 若 [getLayoutRes] 返回0, 则通过重写本方法来替换fragment
     * */
    open fun getContentFragment(): Fragment? = null

    abstract fun afterViewCreated()

    open fun beforeSetContentView() {

    }

    /**
     * 由于fragment嵌套多层,内层fragment沉浸式时,需要为状态栏预留空间
     * @param containerView fragment/activity 的容器布局,通过动态设置其paddingTop值来实现
     * */
    open fun updatePaddingTopWithStatusHeight(containerView: View?) {
        if (containerView == null) {
            LoggerUtil.d("updatePaddingTopWithStatusHeight fail  $containerView $this")
            return
        }

        val height = ScreenUtil.getStatusBarHeightPx(this)
        // Logger.d("height $height")
        containerView.setPadding(
            containerView.paddingLeft, containerView.paddingTop + height,
            containerView.paddingRight, containerView.paddingBottom
        )
    }
//    override fun getResources(): Resources { //还原字体大小
//        val res: Resources = super.getResources()
//        val configuration = res.configuration.apply { fontScale = 1.0f }
////        createConfigurationContext(configuration)
//        res.updateConfiguration(configuration, res.getDisplayMetrics())
//        return res
//    }
}