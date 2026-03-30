"use client";

import Link from "next/link";
import SearchCommand from "@/components/SearchCommand";
import UserDropdown from "@/components/UserDropdown";

const Header = ({ user, initialStocks }: { user: User; initialStocks: StockWithWatchlistStatus[] }) => {
    return (
        <header className="sticky top-0 header">
            <div className="container header-wrapper">
                <Link href="/" className="header-brand">
                    <span className="header-brand-name">Stockify</span>
                </Link>

                <div className="header-search-area">
                    <SearchCommand renderAs="header" label="Search stocks" initialStocks={initialStocks} />
                </div>

                <UserDropdown user={user} initialStocks={initialStocks} />
            </div>
        </header>
    )
}
export default Header
