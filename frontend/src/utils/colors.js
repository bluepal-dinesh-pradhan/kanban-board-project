// Board background gradients
export const BOARD_BACKGROUNDS = [
  { name: 'Blue', value: '#0079BF', gradient: 'linear-gradient(180deg, #0079BF 0%, #5BA4CF 100%)' },
  { name: 'Green', value: '#519839', gradient: 'linear-gradient(180deg, #519839 0%, #61BD4F 100%)' },
  { name: 'Purple', value: '#89609E', gradient: 'linear-gradient(180deg, #89609E 0%, #CD8DE5 100%)' },
  { name: 'Red', value: '#B04632', gradient: 'linear-gradient(180deg, #B04632 0%, #EB5A46 100%)' },
  { name: 'Orange', value: '#D29034', gradient: 'linear-gradient(180deg, #D29034 0%, #FFAB4A 100%)' }
]

// Card label colors (Trello standard)
export const LABEL_COLORS = [
  { name: 'Green', value: '#61BD4F' },
  { name: 'Yellow', value: '#F2D600' },
  { name: 'Orange', value: '#FF9F1A' },
  { name: 'Red', value: '#EB5A46' },
  { name: 'Purple', value: '#C377E0' },
  { name: 'Blue', value: '#0079BF' }
]

// Generate avatar background color based on name
export const getAvatarColor = (name) => {
  const colors = [
    '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7',
    '#DDA0DD', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9'
  ]
  
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  
  return colors[Math.abs(hash) % colors.length]
}

// Get initials from full name
export const getInitials = (fullName) => {
  if (!fullName) return '?'
  
  const names = fullName.trim().split(' ')
  if (names.length === 1) {
    return names[0].charAt(0).toUpperCase()
  }
  
  return (names[0].charAt(0) + names[names.length - 1].charAt(0)).toUpperCase()
}

// Get board background gradient from color value
export const getBoardGradient = (colorValue) => {
  const background = BOARD_BACKGROUNDS.find(bg => bg.value === colorValue)
  return background ? background.gradient : BOARD_BACKGROUNDS[0].gradient
}

// Generate gradient class name from board name (fallback)
export const getGradientFromName = (name) => {
  const colors = ['from-blue-500 to-blue-600', 'from-green-500 to-green-600', 'from-purple-500 to-purple-600', 'from-red-500 to-red-600', 'from-orange-500 to-orange-600']
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}