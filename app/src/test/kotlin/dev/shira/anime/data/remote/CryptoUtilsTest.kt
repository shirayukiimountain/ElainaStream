package dev.shira.anime.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptoUtilsTest {

    private val secretKey = "4a8cdde36958771afd8b3a4eefe51dd367a83862dc30cb48a5bb13131669c2ad"

    @Test
    fun testDecryptFhdUrl() {
        val encryptedFhd = "KdXMPTyOEJicwKMo1kp9y3iONSVQPMsm8oO4Oi2biMQEpVfQrqqbyiojboS3rML3gJ2NCelCg/nGHovIoHoObbDWtlANAs4IMhfuoxEuEONq6aBFvppZ4Jj/EG2JNbreYiNd1VNlYpYRzs8sZJ2juDUpHaFkY4F4fQ0lxkSs"
        val expected = "http://peach.whatbox.ca:24591/xyz/03_episode/Tensei_Kizoku_Kantei_Skill_de_Nariagaru_S3/FHD/01.mp4"

        val result = CryptoUtils.decrypt(encryptedFhd, secretKey)
        assertEquals(expected, result)
    }

    @Test
    fun testDecryptGdriveUrl() {
        val encryptedGdrive = "0dcE/UTzKUk/asho6BkiHiqfa7tTCKrm+nSNXl3e2Mnb/4/+CW4Om6L6eiVKL/pLWhf/5sV4Tm3V3i39Y5GoB7iZ08/zcjBb1DMNbbLYZO+s4BXZD2Y6fbBdgoojOwY/INJwowFG5mz9Vzp0ofBQay4="
        val expected = "https://drive.google.com/file/d/1YUj5xI40GsdK1hqCdzkyJWAWB7fYhcHd/view?usp=drive_link"

        val result = CryptoUtils.decrypt(encryptedGdrive, secretKey)
        assertEquals(expected, result)
    }

    @Test
    fun testDecryptSdUrl() {
        val encryptedSd = "jZOQtumSmo9r+ghqjDhc7guwDlRajpYF/iyPoyxTNXqTH4iN8s6F+4+1H9AWxUl0pL5WLmsBXiLsPa6gWV6bk7T6+BARkmT6UJvsGS905pLEG0I6WN+2kEFjFpvP6WC0Qht/f80GSVgKaJThOZm9WOFpBs1bMvch+iQIy00BNn2aNp0nqFIxOn+Ylls3Y78Gd3ZxK/ABYxneZOZwmNatyKv6mVZSIQ6DFv+M2G2jYdRu3VJfMJarL4T1D2lQKk54ymAvEKbtAdK8c9pMhxPUlVRaqxtN3niWJCqxvWS97DK/VsSj1eMFWVBF1BwXT6sCO5BsjPvUEa3slSZZNlHEfi0VLv9kN+85Shtqqr+HWubaTKN4BABqwbg6Vg4KJQN6Rzaj/ULMa8g+Dd1LqXky2kyhH61Dzobgek7tV+Hk2GZrbRbwW/+Dnwea1s/KjAJLy3hzRv/JBNAWY5H/7aSw+AH61mb+7AENG59XHlgRPUSjOwRF0cp0yLvHR26Lz5BrNnMTuDlMfmLezJfHI1kNn/BrfbwS+4ZwkmTYDRwUgzlP1jl17FoYSpUNOhrkbDDEsZtVzhoNO7MLijRm26Y2GnWq85gdMQmFuZ86HTnFV810quFRqy3YOu+nHDV+FZtA0tNEg07xm117yYgjgt53zKpWnZAB7M2A9PAWViCfFkS+xaNN5WaisFh4tYhwYp3m9fHzA3OssBz5UGpo7PU7XTtWm60lysiPjG+CZVKmfUD6xpR20HfR66LjGmRvy7uSKMsp81bHKpR/ljKMBxMXSenc+Kp2c2WrmpKVTjZjpAFm0UDLUWTfIeNyOawbKgzIMrhShThMRcT6tJzJlT74f3L7XejPBjtrfxjYQKtDnHWRuKLZ34afV/uayObs/pMN4TM5UJEvGIWZV0iAYfkiYWPYZJG2NPspnoV67YBFJHdO5OdJyxjb+XPxAcT98QhZfgNFue9B6Ntmt5KyN+EwT2D0IgLDZGp1TzOcP+GpPegR+d04H3/7BJlOoJcpBmybhMKLGBluC4z/Tnxz79XoZwyi57osEOJsvnnTgXW+QPTKUtuhWfhbzS+vWWZZNv/bL2R3tO2xveSIwz9HpM8xPjiupWPB41BdOniz4GedCCO3v2BFH/KpQv4QoBMN7rsqXbUHaKNtHzQ0kh9gXWpUX46LcGIml+OTEbA83jkhFC+jzP3Gg6qwGpXYURm0h7PrwhswXe0XSdZw/Esyl1w1MZJYdBQgOM/XVekm6VTBbN/UW1ZdS87LCXXoAp5q1icUtsNmiesd6T1M/kY9wY/XzUG0hOn/DyMbwCj7pFRivpBqNCYlwm+9BGQkhV/7CVrAwmTzVap8fOFxRTJhC0cxZrV+qdHaYxda0ZaS/sReszHbmi2F6odEVAB7SMYFWQtZ4HNU5XPLpdfq33vrTLDDwcvncb96OHBPTK9kZVyG8QFOf8cJKo7XQcZ06vdfFyDHXA/aMs+V2FMy/0SPO6wtIQUNAf1bZtlmVq7btknLQRnrZPRDmIBmNbMp2qQ3Z+cZwIqi07bxm4ZNpQ9Np+a84U4A0aMmjgnTYncZDEtA2HGXX9USndg="
        val result = CryptoUtils.decrypt(encryptedSd, secretKey)
        assertTrue(result.startsWith("https://scontent-sin11-1.xx.fbcdn.net/"))
        assertTrue(result.contains(".mp4"))
    }

    @Test
    fun testPlainTextPassThrough() {
        val plain = "https://example.com/video.mp4"
        val result = CryptoUtils.decrypt(plain, secretKey)
        assertEquals(plain, result)
    }
}
