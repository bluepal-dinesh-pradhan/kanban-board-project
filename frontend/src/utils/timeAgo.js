export const timeAgo = (date) => {
  if (!date) return ''
  
  const now = new Date()
  const past = new Date(date)
  const diffInSeconds = Math.floor((now - past) / 1000)
  
  if (diffInSeconds < 60) {
    return 'just now'
  }
  
  const diffInMinutes = Math.floor(diffInSeconds / 60)
  if (diffInMinutes < 60) {
    return `${diffInMinutes} min ago`
  }
  
  const diffInHours = Math.floor(diffInMinutes / 60)
  if (diffInHours < 24) {
    return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`
  }
  
  const diffInDays = Math.floor(diffInHours / 24)
  if (diffInDays === 1) {
    return 'yesterday'
  }
  
  if (diffInDays < 7) {
    return `${diffInDays} days ago`
  }
  
  // For older dates, show formatted date
  const options = { month: 'short', day: 'numeric' }
  if (past.getFullYear() !== now.getFullYear()) {
    options.year = 'numeric'
  }
  
  return past.toLocaleDateString('en-US', options)
}