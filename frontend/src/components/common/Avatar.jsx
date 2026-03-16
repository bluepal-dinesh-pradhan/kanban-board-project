import { getAvatarColor, getInitials } from '../../utils/avatarColor'

const Avatar = ({ 
  name, 
  size = 'md', 
  className = '',
  showTooltip = true 
}) => {
  const sizeClasses = {
    sm: 'w-7 h-7 text-xs',
    md: 'w-9 h-9 text-sm', 
    lg: 'w-12 h-12 text-base'
  }
  
  const backgroundColor = getAvatarColor(name)
  const initials = getInitials(name)
  
  const avatar = (
    <div 
      className={`${sizeClasses[size]} rounded-full flex items-center justify-center text-white font-semibold select-none ${className}`}
      style={{ backgroundColor }}
      title={showTooltip ? name : undefined}
    >
      {initials}
    </div>
  )
  
  return avatar
}

export default Avatar