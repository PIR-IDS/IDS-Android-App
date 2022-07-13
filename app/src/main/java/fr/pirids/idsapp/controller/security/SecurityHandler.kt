package fr.pirids.idsapp.controller.security

import android.content.Context
import androidx.security.crypto.MasterKey
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.integration.android.AndroidKeystoreKmsClient
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

object SecurityHandler {
    private const val masterKeyAlias = MasterKey.DEFAULT_MASTER_KEY_ALIAS
    private var masterKey: MasterKey? = null

    private fun buildMasterKey(context: Context): MasterKey =
        MasterKey.Builder(context, masterKeyAlias)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    fun getAead(context: Context): Aead {
        if(masterKey == null) {
            masterKey = buildMasterKey(context)
        }
        AeadConfig.register()
        return AndroidKeysetManager.Builder()
            .withMasterKeyUri(AndroidKeystoreKmsClient.PREFIX + masterKeyAlias)
            .withSharedPref(context, "master_keyset", "master_key_preference")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .build()
            .keysetHandle
            .getPrimitive(Aead::class.java)
    }

    /**
     * Generates a random 32 byte key. Will be used to create a passphrase for SQLCipher.
     * Note: This will produce a pseudo random number.
     * The issue is on some OEMs and APIs, there is not enough entropy
     * which can result in a vulnerability where the random number isn’t
     * random enough and can be predictable.
     * This affects all Java APIs on Android. In most cases this is sufficient.
     * @return a byte array containing random values
     */
    fun generateRandomKey(): ByteArray =
        ByteArray(32).apply {
            SecureRandom
                .getInstanceStrong()
                //TODO: maybe use this conversion method to get a Kotlin Random object?
                //.asKotlinRandom()
                .nextBytes(this)
        }
}