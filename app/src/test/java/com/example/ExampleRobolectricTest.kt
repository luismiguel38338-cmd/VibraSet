package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    val appDescription = context.getString(R.string.app_description)
    val appAuthor = context.getString(R.string.app_author)

    assertEquals("VibraSet", appName)
    assertEquals("Ecualizador y configurador de audio profesional para Android.", appDescription)
    assertEquals("Luis Miguel Martínez Cabrera", appAuthor)
  }

  @Test
  fun `verify official vibraset logo drawable exists`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val drawable = context.getDrawable(R.drawable.vibraset_logo)
    org.junit.Assert.assertNotNull(drawable)
  }
}
