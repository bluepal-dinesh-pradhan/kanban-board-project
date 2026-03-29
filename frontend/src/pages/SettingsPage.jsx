import { useState, useMemo } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { 
  FiHome, FiGrid, FiChevronDown, FiUsers, FiSettings, 
  FiUser, FiBell, FiLock, FiEye, FiEyeOff, FiSave, FiCheckCircle 
} from 'react-icons/fi'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import Avatar from '../components/common/Avatar'
import { userAPI } from '../api/endpoints'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'

const Toggle = ({ enabled, onChange, label, isLoading }) => (
  <div className="flex items-center justify-between py-3">
    <span className="text-sm font-medium text-slate-700">{label}</span>
    <button
      disabled={isLoading}
      onClick={() => onChange(!enabled)}
      className={`${
        enabled ? 'bg-blue-600' : 'bg-slate-200'
      } relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none disabled:opacity-50`}
    >
      <span
        className={`${
          enabled ? 'translate-x-5' : 'translate-x-0'
        } pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out`}
      />
    </button>
  </div>
)

const PasswordStrengthIndicator = ({ password }) => {
  const getStrength = (p) => {
    if (!p) return { label: '', color: 'bg-slate-200', width: '0%', text: '' }
    if (p.length < 8) return { label: 'Weak', color: 'bg-rose-500', width: '33%', text: 'text-rose-600' }
    
    const hasUpper = /[A-Z]/.test(p)
    const hasLower = /[a-z]/.test(p)
    const hasNumber = /\d/.test(p)
    const hasSpecial = /[@$!%*?&#]/.test(p)
    
    const criteriaCount = [hasUpper, hasLower, hasNumber, hasSpecial].filter(Boolean).length
    
    if (criteriaCount < 4) return { label: 'Good', color: 'bg-amber-500', width: '66%', text: 'text-amber-600' }
    return { label: 'Strong', color: 'bg-emerald-500', width: '100%', text: 'text-emerald-600' }
  }

  const strength = getStrength(password)

  if (!password) return null

  return (
    <div className="mt-2 space-y-1.5">
      <div className="flex justify-between items-center">
        <span className={`text-[10px] font-bold uppercase tracking-wider ${strength.text}`}>
          Strength: {strength.label}
        </span>
      </div>
      <div className="h-1 w-full bg-slate-100 rounded-full overflow-hidden">
        <div 
          className={`h-full ${strength.color} transition-all duration-500 ease-out`} 
          style={{ width: strength.width }}
        />
      </div>
    </div>
  )
}

const PasswordInput = ({ label, value, onChange, placeholder, error, children }) => {
  const [show, setShow] = useState(false)
  return (
    <div className="space-y-1">
      <label className="block text-sm font-semibold text-slate-700">{label}</label>
      <div className="relative">
        <input
          type={show ? 'text' : 'password'}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          className={`w-full px-4 py-2.5 rounded-lg border ${
            error ? 'border-rose-500' : 'border-slate-300'
          } focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all text-sm`}
        />
        <button
          type="button"
          onClick={() => setShow(!show)}
          className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
        >
          {show ? <FiEyeOff className="w-5 h-5" /> : <FiEye className="w-5 h-5" />}
        </button>
      </div>
      {children}
      {error && <p className="text-xs text-rose-600 font-medium">{error}</p>}
    </div>
  )
}

const SettingsPage = () => {
  const { user: authUser, setUser } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  
  const [fullName, setFullName] = useState('')
  const [passwords, setPasswords] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  const [passwordErrors, setPasswordErrors] = useState({})

  const activeNav = 'settings'

  const { data: profile, isLoading: isProfileLoading } = useQuery({
    queryKey: ['user', 'profile'],
    queryFn: async () => {
      const response = await userAPI.getProfile()
      const data = response.data
      setFullName(data.fullName)
      return data
    }
  })

  const updateProfileMutation = useMutation({
    mutationFn: (data) => userAPI.updateProfile(data),
    onSuccess: () => {
      // Update localStorage with new user data
      const currentUser = JSON.parse(localStorage.getItem('user'))
      if (currentUser) {
        currentUser.fullName = fullName
        localStorage.setItem('user', JSON.stringify(currentUser))
        
        // Update AuthContext state
        if (setUser) {
          setUser(currentUser)
        }
      }

      queryClient.invalidateQueries(['user', 'profile'])
      toast.success('Profile updated successfully')
    },
    onError: () => {
      toast.error('Failed to update profile')
    }
  })

  const updatePrefsMutation = useMutation({
    mutationFn: (data) => userAPI.updateNotificationPreferences(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['user', 'profile'])
      toast.success('Preferences updated')
    },
    onError: () => {
      toast.error('Failed to update preferences')
    }
  })

  const changePasswordMutation = useMutation({
    mutationFn: (data) => userAPI.changePassword(data),
    onSuccess: () => {
      setPasswords({ currentPassword: '', newPassword: '', confirmPassword: '' })
      setPasswordErrors({})
      toast.success('Password changed successfully')
    },
    onError: (error) => {
      const message = error.response?.data?.message || 'Failed to change password'
      if (message.toLowerCase().includes('current password')) {
        setPasswordErrors({ currentPassword: 'Current password is incorrect' })
      } else {
        toast.error(message)
      }
    }
  })

  const handleProfileSave = () => {
    if (!fullName.trim()) {
      toast.error('Name cannot be empty')
      return
    }
    updateProfileMutation.mutate({ fullName })
  }

  const handlePrefToggle = (key, value) => {
    const newPrefs = {
      emailNotifications: profile.emailNotifications,
      dueDateReminders: profile.dueDateReminders,
      boardInvitationEmails: profile.boardInvitationEmails,
      [key]: value
    }
    updatePrefsMutation.mutate(newPrefs)
  }

  const handlePasswordUpdate = () => {
    const errors = {}
    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]{8,}$/

    if (!passwords.currentPassword) {
      errors.currentPassword = 'Required'
    }

    if (!passwords.newPassword) {
      errors.newPassword = 'Required'
    } else if (passwords.newPassword === passwords.currentPassword) {
      errors.newPassword = 'New password must be different from current password'
    } else if (!passwordPattern.test(passwords.newPassword)) {
      errors.newPassword = 'Password must be at least 8 characters with uppercase, lowercase, number, and special character'
    }
    
    if (!passwords.confirmPassword) {
      errors.confirmPassword = 'Please confirm your password'
    } else if (passwords.newPassword !== passwords.confirmPassword) {
      errors.confirmPassword = 'Passwords do not match'
    }

    if (Object.keys(errors).length > 0) {
      setPasswordErrors(errors)
      return
    }

    changePasswordMutation.mutate({
      currentPassword: passwords.currentPassword,
      newPassword: passwords.newPassword
    })
  }

  if (isProfileLoading) {
    return (
      <div className="min-h-screen bg-[#F4F5F7]">
        <Navbar />
        <div className="max-w-[1400px] mx-auto flex">
          <div className="flex-1 flex items-center justify-center h-[calc(100vh-48px)]">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
          </div>
        </div>
      </div>
    )
  }

  const initials = fullName?.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) || '??'

  return (
    <div className="min-h-screen bg-[#F4F5F7]">
      <Navbar />

      <div className="max-w-[1400px] mx-auto flex">
        {/* Sidebar */}
        <Sidebar />

        {/* Main Content */}
        <main className="flex-1 px-8 py-10 max-w-4xl">
          <header className="mb-10">
            <h1 className="text-2xl font-bold text-slate-900">Account Settings</h1>
            <p className="text-slate-500 mt-1">Manage your profile, notifications, and security</p>
          </header>

          <div className="space-y-8">
            {/* Profile Section */}
            <section className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
              <div className="px-6 py-4 border-b border-slate-100 flex items-center gap-2">
                <FiUser className="text-blue-600 w-5 h-5" />
                <h2 className="text-lg font-bold text-slate-900">Profile Information</h2>
              </div>
              <div className="p-8">
                <div className="flex flex-col md:flex-row items-start md:items-center gap-8 mb-8">
                  <div className="w-24 h-24 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center text-3xl font-bold border-4 border-white shadow-md ring-1 ring-blue-50">
                    {initials}
                  </div>
                  <div className="space-y-1">
                    <p className="text-lg font-bold text-slate-900">{profile.fullName}</p>
                    <p className="text-sm text-slate-500 flex items-center gap-1.5">
                      <FiCheckCircle className="text-emerald-500" /> Professional Plan
                    </p>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-1.5">
                    <label className="block text-sm font-semibold text-slate-700">Full Name</label>
                    <input
                      type="text"
                      val={fullName}
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                      className="w-full px-4 py-2.5 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all text-sm"
                    />
                  </div>
                  <div className="space-y-1.5 opacity-60 cursor-not-allowed">
                    <label className="block text-sm font-semibold text-slate-700">Email Address (Read-only)</label>
                    <input
                      type="email"
                      value={profile.email}
                      readOnly
                      className="w-full px-4 py-2.5 rounded-lg border border-slate-200 bg-slate-50 text-slate-500 text-sm cursor-not-allowed"
                    />
                  </div>
                </div>

                <div className="mt-8 pt-6 border-t border-slate-100 flex justify-end">
                  <button
                    onClick={handleProfileSave}
                    disabled={updateProfileMutation.isPending}
                    className="flex items-center gap-2 px-6 py-2.5 bg-blue-600 text-white text-sm font-bold rounded-lg hover:bg-blue-700 transition-all shadow-md active:scale-95 disabled:opacity-50"
                  >
                    {updateProfileMutation.isPending ? 'Saving...' : (
                      <>
                        <FiSave />
                        Save Changes
                      </>
                    )}
                  </button>
                </div>
              </div>
            </section>

            {/* Notification Preferences Section */}
            <section className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
              <div className="px-6 py-4 border-b border-slate-100 flex items-center gap-2">
                <FiBell className="text-blue-600 w-5 h-5" />
                <h2 className="text-lg font-bold text-slate-900">Notification Preferences</h2>
              </div>
              <div className="p-8 divide-y divide-slate-100">
                <Toggle
                  label="Email Notifications"
                  enabled={profile.emailNotifications}
                  onChange={(v) => handlePrefToggle('emailNotifications', v)}
                  isLoading={updatePrefsMutation.isPending}
                />
                <Toggle
                  label="Due Date Reminders"
                  enabled={profile.dueDateReminders}
                  onChange={(v) => handlePrefToggle('dueDateReminders', v)}
                  isLoading={updatePrefsMutation.isPending}
                />
                <Toggle
                  label="Board Invitation Emails"
                  enabled={profile.boardInvitationEmails}
                  onChange={(v) => handlePrefToggle('boardInvitationEmails', v)}
                  isLoading={updatePrefsMutation.isPending}
                />
              </div>
            </section>

            {/* Change Password Section */}
            <section className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
              <div className="px-6 py-4 border-b border-slate-100 flex items-center gap-2">
                <FiLock className="text-blue-600 w-5 h-5" />
                <h2 className="text-lg font-bold text-slate-900">Change Password</h2>
              </div>
              <div className="p-8">
                <div className="max-w-md space-y-5">
                  <PasswordInput
                    label="Current Password"
                    value={passwords.currentPassword}
                    onChange={(v) => {
                      setPasswords({...passwords, currentPassword: v})
                      if (passwordErrors.currentPassword) {
                        const newErrors = { ...passwordErrors }
                        delete newErrors.currentPassword
                        setPasswordErrors(newErrors)
                      }
                    }}
                    placeholder="Enter current password"
                    error={passwordErrors.currentPassword}
                  />
                  <div className="h-px bg-slate-100 my-2"></div>
                  <PasswordInput
                    label="New Password"
                    value={passwords.newPassword}
                    onChange={(v) => {
                      setPasswords({...passwords, newPassword: v})
                      if (passwordErrors.newPassword) {
                        const newErrors = { ...passwordErrors }
                        delete newErrors.newPassword
                        setPasswordErrors(newErrors)
                      }
                    }}
                    placeholder="Min 8 characters + symbols"
                    error={passwordErrors.newPassword}
                  >
                    <PasswordStrengthIndicator password={passwords.newPassword} />
                  </PasswordInput>
                  <PasswordInput
                    label="Confirm New Password"
                    value={passwords.confirmPassword}
                    onChange={(v) => {
                      setPasswords({...passwords, confirmPassword: v})
                      if (passwordErrors.confirmPassword) {
                        const newErrors = { ...passwordErrors }
                        delete newErrors.confirmPassword
                        setPasswordErrors(newErrors)
                      }
                    }}
                    placeholder="Re-type new password"
                    error={passwordErrors.confirmPassword}
                  />
                  
                  <div className="pt-4">
                    <button
                      onClick={handlePasswordUpdate}
                      disabled={changePasswordMutation.isPending}
                      className="w-full md:w-auto px-8 py-2.5 bg-slate-900 text-white text-sm font-bold rounded-lg hover:bg-slate-800 transition-all shadow-md active:scale-95 disabled:opacity-50"
                    >
                      {changePasswordMutation.isPending ? 'Updating...' : 'Update Password'}
                    </button>
                  </div>
                </div>
              </div>
            </section>
          </div>
        </main>
      </div>
    </div>
  )
}

export default SettingsPage
