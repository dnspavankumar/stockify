import ProtectedAppShell from "@/components/ProtectedAppShell";

const Layout = ({ children }: { children : React.ReactNode }) => {
    return <ProtectedAppShell>{children}</ProtectedAppShell>
}
export default Layout
