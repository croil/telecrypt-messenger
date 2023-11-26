package tele.crypt.telecrypt

import android.content.Context
import android.content.SharedPreferences
import tele.crypt.recycler.User

class SessionManager(
    context: Context
) {
    private val preference: SharedPreferences = context.getSharedPreferences(
        Constants.SHARED_PREFERENCE,
        Context.MODE_PRIVATE
    )
    private val editor: SharedPreferences.Editor = preference.edit()
    fun getEditor(): SharedPreferences.Editor {
        return this.editor
    }

    fun saveSession(id : String) {
        editor.putString(Constants.SESSION, id).apply()
    }

    fun getSession(): String? {
        return preference.getString(Constants.SESSION, null)
    }

    fun removeSession() {
        editor.putString(Constants.SESSION, null).apply()
    }
}