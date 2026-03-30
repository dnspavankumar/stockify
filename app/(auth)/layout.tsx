import Link from "next/link";
import Image from "next/image";
import GuestOnly from "@/components/GuestOnly";

const Layout = ({ children }: { children : React.ReactNode }) => {
    return (
        <main className="auth-layout">
            <div className="landing-visual" aria-hidden="true">
                <Image
                    src="/assets/images/landing.jpg"
                    alt="Abstract landing background"
                    fill
                    sizes="100vw"
                    className="landing-visual-image"
                    priority
                />
                <span className="landing-visual-glass" />
                <span className="landing-visual-noise" />
            </div>

            <div className="auth-stage">
                <Link href="/" className="auth-logo inline-flex items-center text-lg font-semibold uppercase tracking-[0.24em] text-gray-100">
                    Stockify
                </Link>

                <section className="auth-shell">
                    <div className="auth-card scrollbar-hide-default">
                        <GuestOnly>{children}</GuestOnly>
                    </div>
                </section>
            </div>
        </main>
    )
}
export default Layout
