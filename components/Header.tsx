"use client";

import Link from "next/link";
import NavItems from "@/components/NavItems";
import UserDropdown from "@/components/UserDropdown";

const Header = ({ user, initialStocks }: { user: User; initialStocks: StockWithWatchlistStatus[] }) => {
    return (
        <header className="sticky top-0 header">
            <div className="container header-wrapper">
                <Link href="/" className="text-base font-semibold uppercase tracking-[0.2em] text-gray-100 transition-colors hover:text-yellow-500">
                    Stockify
                </Link>
                <nav className="hidden sm:block">
                    <NavItems initialStocks={initialStocks} />
                </nav>

                <UserDropdown user={user} initialStocks={initialStocks} />
            </div>
        </header>
    )
}
export default Header
