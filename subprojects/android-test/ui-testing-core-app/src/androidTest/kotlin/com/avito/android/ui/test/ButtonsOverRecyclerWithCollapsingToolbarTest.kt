package com.avito.android.ui.test

import android.util.Log
import com.avito.android.test.annotations.UIComponentTest
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.ButtonsOverRecyclerWithCollapsingToolbarActivity
import org.junit.Rule
import org.junit.Test

class ButtonsOverRecyclerWithCollapsingToolbarTest {

    @get:Rule
    val rule = screenRule<ButtonsOverRecyclerWithCollapsingToolbarActivity>(launchActivity = true)


    @UIComponentTest
    @Test
    fun listElement_elementClicked_whenThereIsOverlappedButtonInScreenWithCollapsingToolbar() {
        Screen.buttonsOverRecycler.list.cellAt(90).click()
        Log.i("testy", "itesty listElement_elementClicked_whenThereIsOverlappedButtonInScreenWithCollapsingToolbar")
        Log.d("testy", "dtesty listElement_elementClicked_whenThereIsOverlappedButtonInScreenWithCollapsingToolbar")
        Log.e("testy", "etesty listElement_elementClicked_whenThereIsOverlappedButtonInScreenWithCollapsingToolbar")
        throw Exception("testys")
    }
}
