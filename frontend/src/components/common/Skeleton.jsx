const Skeleton = ({ className = '', width, height, rounded = false }) => {
  const baseClasses = 'animate-pulse bg-gray-200'
  const roundedClass = rounded ? 'rounded-full' : 'rounded'
  
  const style = {}
  if (width) style.width = width
  if (height) style.height = height
  
  return (
    <div 
      className={`${baseClasses} ${roundedClass} ${className}`}
      style={style}
    />
  )
}

// Preset skeleton components
export const SkeletonText = ({ lines = 1, className = '' }) => (
  <div className={`space-y-2 ${className}`}>
    {Array.from({ length: lines }).map((_, i) => (
      <Skeleton 
        key={i} 
        className="h-4" 
        width={i === lines - 1 ? '75%' : '100%'} 
      />
    ))}
  </div>
)

export const SkeletonCard = ({ className = '' }) => (
  <div className={`p-4 bg-white rounded-lg shadow-sm border ${className}`}>
    <Skeleton className="h-4 mb-3" width="80%" />
    <SkeletonText lines={2} />
    <div className="flex items-center mt-4 space-x-2">
      <Skeleton className="w-6 h-6" rounded />
      <Skeleton className="h-3" width="60px" />
    </div>
  </div>
)

export const SkeletonBoard = ({ className = '' }) => (
  <div className={`p-4 rounded-xl h-24 ${className}`}>
    <Skeleton className="h-5 mb-2" width="70%" />
    <Skeleton className="h-3" width="50%" />
    <div className="flex items-center mt-3 space-x-1">
      <Skeleton className="w-5 h-5" rounded />
      <Skeleton className="w-5 h-5" rounded />
      <Skeleton className="h-3" width="40px" />
    </div>
  </div>
)

export default Skeleton