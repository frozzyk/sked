package kozyriatskyi.anton.sked.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import kozyriatskyi.anton.sked.BuildConfig

/**
 * Created by Anton Kozyriatskyi on 30.07.16.
 */

/**
 * Adds function to ViewGroup, so it can inflate views like this:
 *  myViewGroup.inflate(R.layout.my_layout)
 *  myViewGroup.inflate(R.layout.my_layout, myInflater)
 *  myViewGroup.inflate(R.layout.my_layout, true)
 *  myViewGroup.inflate(R.layout.my_layout, myInflater, true)
 *
 *  In Java it looks like:
 *  ExtensionsKt.inflate(myViewGroup, R.layout.my_layout, myLayoutInflater, true)
 * */
fun ViewGroup.inflate(@LayoutRes layout: Int, inflater: LayoutInflater = LayoutInflater.from(context),
                      attachToRoot: Boolean = false): View =
// 'this' refers to the ViewGroup this function is called on
        inflater.inflate(layout, this, attachToRoot)

/**
 * Adds function to View, so it can find and cast views' in itself
 *
 * Example:
 *  rootView.find<TextView>(R.id.my_text_view)
 * */
@Suppress("UNCHECKED_CAST")
fun <T : View?> View.find(@IdRes id: Int): T = findViewById<T>(id)

/**
 * Adds function to Activity, so it can find and cast views' in itself
 *
 * Example:
 *  find<TextView>(R.id.my_text_view)
 * */
@Suppress("UNCHECKED_CAST")
@Deprecated("", ReplaceWith("findViewById<T>(id)"))
fun <T : View?> Activity.find(@IdRes id: Int): T = findViewById<T>(id)

/**
 * Adds 'toast' functions to Context and android.support.v4.app.Fragment
 *
 * Examples:
 *  toast(myString)
 *  toast(R.string.myString)
 *  toast(myString, Toast.LENGTH_LONG)
 * */
fun Fragment.toast(string: CharSequence, length: Int = Toast.LENGTH_LONG) {
    this.activity?.toast(string, length)
}

fun Fragment.toast(@StringRes id: Int, length: Int = Toast.LENGTH_LONG) {
    this.activity?.toast(id, length)
}

fun Context.toast(string: CharSequence, length: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, string, length).show()
}

fun Context.toast(@StringRes id: Int, length: Int = Toast.LENGTH_LONG) {
    toast(getString(id), length)
}

fun Any.logI(str: String) = Log.i(this.javaClass.simpleName, str)

fun Any.logE(str: String, t: Throwable? = null, tag: String = this.javaClass.simpleName) {
    if (BuildConfig.DEBUG) {
        if (t == null) {
            Log.e(tag, str)
        } else {
            Log.e(tag, str, t)
        }
    }
}

fun Any.logD(str: String, t: Throwable? = null, tag: String = this.javaClass.simpleName) {
    if (BuildConfig.DEBUG) {
        if (t == null) {
            Log.d(tag, str)
        } else {
            Log.d(tag, str, t)
        }
    }
}

/**
 * Functions for managing view visibility
 * */
fun View.setGone() {
    if (this.visibility != View.GONE) this.visibility = View.GONE
}

fun View.setVisible() {
    if (this.visibility != View.VISIBLE) this.visibility = View.VISIBLE
}

fun View.setInvisible() {
    if (this.visibility != View.INVISIBLE) this.visibility = View.INVISIBLE
}

fun View.setEnabled() {
    if (this.isEnabled.not()) this.isEnabled = true
}

fun View.setDisabled() {
    if (this.isEnabled) this.isEnabled = false
}

inline fun SharedPreferences.edit(func: SharedPreferences.Editor.() -> SharedPreferences.Editor) {
    this.edit()
            .func()
            .apply()
}