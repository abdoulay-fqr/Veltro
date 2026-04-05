export default function UnauthorizedPage() {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="text-center">
        <div className="text-3xl font-bold text-purple-600 uppercase tracking-tight mb-4">Veltro</div>
        <h1 className="text-xl font-medium mb-2">Access Denied</h1>
        <p className="text-sm font-light text-gray-500">This dashboard is for admins and coaches only.</p>
      </div>
    </div>
  );
}