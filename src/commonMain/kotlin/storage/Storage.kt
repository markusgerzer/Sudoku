package storage

import com.soywiz.korge.service.storage.NativeStorage
import com.soywiz.korge.service.storage.get
import com.soywiz.korio.serialization.json.Json
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


object Storage {
    lateinit var nativeStorage: NativeStorage

    fun <T> dataStorage(
        defaultValue: T,
        loadFromStorage: Boolean = true,
        func: (Any?) -> T = { it as T }
    ) =
        PropertyDelegateProvider<Storable, ReadWriteProperty<Storable, T>> { thisRef, property ->
            object : ReadWriteProperty<Storable, T> {
                val key = "${thisRef.storageKey}.${property.name}"
                init {
                    if (nativeStorage.getOrNull(key) == null || !loadFromStorage)
                        write(key, defaultValue as Any)
                }

                override fun getValue(thisRef: Storable, property: KProperty<*>) =
                    func(read(key))

                override fun setValue(thisRef: Storable, property: KProperty<*>, value: T) =
                    write(key, value as Any)
            }
        }

    interface Storable { val storageKey: String }

    interface SelfStorable: Storable, Json.CustomSerializer {
        fun saveToStorage() = write(storageKey, this)
    }

    fun loadFromStorage(storageKey: String) = read(storageKey)

    private fun write(key: String, value: Any) {
        val jsonString = Json.stringify(value)
        nativeStorage[key] = jsonString
        println("$key written to NativeStorage: $jsonString")
    }

    private fun read(key: String): Any? {
        val jsonString = nativeStorage[key]
        println("$key read from NativeStorage: $jsonString")
        return Json.parse(jsonString)
    }
}