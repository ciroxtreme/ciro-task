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
    assertEquals("Ciro Task", appName)
  }

  @Test
  fun `subTaskConverter serializes and deserializes correctly`() {
    val subtasks = listOf(
      com.example.data.model.SubTask(id = "1", title = "Write unit tests", isCompleted = false),
      com.example.data.model.SubTask(id = "2", title = "Build APK", isCompleted = true)
    )
    val json = com.example.data.model.SubTaskConverter.toJson(subtasks)
    val deserialized = com.example.data.model.SubTaskConverter.fromJson(json)

    assertEquals(2, deserialized.size)
    assertEquals("Write unit tests", deserialized[0].title)
    org.junit.Assert.assertFalse(deserialized[0].isCompleted)
    assertEquals("Build APK", deserialized[1].title)
    org.junit.Assert.assertTrue(deserialized[1].isCompleted)
  }
}
