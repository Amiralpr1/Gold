package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.model.AdItem
import com.example.data.repository.AdRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

enum class UserRole {
    NONE, ADMIN, CUSTOMER
}

class AdViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AdRepository(application)

    val allAds: StateFlow<List<AdItem>> = repository.adsFlow

    val activeAds: StateFlow<List<AdItem>> = repository.adsFlow
        .map { list -> list.filter { it.isActive } }
        .let { flow ->
            val stateFlow = MutableStateFlow<List<AdItem>>(emptyList())
            kotlinx.coroutines.GlobalScope
            // We can also collect in viewModelScope
            stateFlow
        }

    private val _userRole = MutableStateFlow(UserRole.NONE)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _loggedInPin = MutableStateFlow("")
    val loggedInPin: StateFlow<String> = _loggedInPin.asStateFlow()

    private val _editingAd = MutableStateFlow<AdItem?>(null)
    val editingAd: StateFlow<AdItem?> = _editingAd.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    fun loginWithCredentials(user: String, pass: String): Boolean {
        val u = user.trim()
        val p = pass.trim()

        // Admin login check
        if ((u.equals("admin", ignoreCase = true) || u == "مدیر") && (p == "7788" || p == "admin")) {
            _userRole.value = UserRole.ADMIN
            _loggedInPin.value = "7788"
            _loginError.value = null
            _editingAd.value = repository.loadAds().firstOrNull()
            return true
        }

        // Customer login check
        val ads = repository.loadAds()
        val matchingAd = ads.find {
            (it.username.equals(u, ignoreCase = true) || it.customerPin == u || it.id == u) &&
                    (it.password == p || it.customerPin == p)
        } ?: ads.find { it.customerPin == p || it.customerPin == u }

        if (matchingAd != null) {
            _userRole.value = UserRole.CUSTOMER
            _loggedInPin.value = matchingAd.customerPin
            _editingAd.value = matchingAd
            _loginError.value = null
            return true
        }

        _loginError.value = "نام کاربری یا رمز عبور اشتباه است.\n(مدیریت: admin / 7788 | مشتریان: zarrin / 1001)"
        return false
    }

    fun loginWithPin(pin: String): Boolean {
        return loginWithCredentials(pin, pin)
    }

    fun selectAdForEditing(ad: AdItem) {
        _editingAd.value = ad
    }

    fun updateEditingAd(updated: AdItem) {
        _editingAd.value = updated
    }

    fun saveEditingAd() {
        val ad = _editingAd.value ?: return
        repository.saveSingleAd(ad)
    }

    fun createNewAd(): AdItem {
        val newAd = AdItem(
            id = "slide_${System.currentTimeMillis()}",
            title = "نام آگهی جدید ✨",
            subtitle = "دسته بندی آگهی 🌟",
            content = "✨ متن کامل آگهی شما اینجا قرار می‌گیرد.\n📞 تلفن: 09120000000\n🌐 وب‌سایت: www.example.com",
            iconName = "Campaign",
            accentColorHex = "#F59E0B",
            bgImageUrl = "https://images.unsplash.com/photo-1610375461246-83df859d849d?auto=format&fit=crop&w=600&q=80",
            customerPin = (1006..9999).random().toString(),
            isActive = true,
            displayOrder = repository.loadAds().size
        )
        repository.saveSingleAd(newAd)
        _editingAd.value = newAd
        return newAd
    }

    fun deleteAd(id: String) {
        repository.deleteAd(id)
        if (_editingAd.value?.id == id) {
            _editingAd.value = repository.loadAds().firstOrNull()
        }
    }

    fun toggleAdActive(id: String) {
        repository.toggleAdActive(id)
    }

    fun logout() {
        _userRole.value = UserRole.NONE
        _loggedInPin.value = ""
        _editingAd.value = null
        _loginError.value = null
    }

    fun resetToDefaults() {
        repository.resetToDefaults()
        if (_userRole.value == UserRole.ADMIN) {
            _editingAd.value = repository.loadAds().firstOrNull()
        }
    }
}
