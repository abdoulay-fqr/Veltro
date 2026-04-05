"use client";

export default function AuthSkeleton() {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="w-full max-w-md px-4">
        {/* Logo skeleton */}
        <div className="text-center mb-8">
          <div className="h-8 w-24 bg-gray-200 rounded-md animate-pulse mx-auto mb-2" />
          <div className="h-4 w-40 bg-gray-100 rounded animate-pulse mx-auto" />
        </div>

        {/* Card skeleton */}
        <div className="bg-white border border-gray-100 rounded-2xl p-8 shadow-sm">
          <div className="h-6 w-36 bg-gray-200 rounded animate-pulse mb-2" />
          <div className="h-4 w-52 bg-gray-100 rounded animate-pulse mb-6" />

          <div className="flex flex-col gap-4">
            <div>
              <div className="h-3 w-10 bg-gray-100 rounded animate-pulse mb-1.5" />
              <div className="h-10 w-full bg-gray-100 rounded-lg animate-pulse" />
            </div>
            <div>
              <div className="h-3 w-14 bg-gray-100 rounded animate-pulse mb-1.5" />
              <div className="h-10 w-full bg-gray-100 rounded-lg animate-pulse" />
            </div>
            <div className="h-10 w-full bg-purple-100 rounded-lg animate-pulse mt-1" />
          </div>
        </div>
      </div>
    </div>
  );
}