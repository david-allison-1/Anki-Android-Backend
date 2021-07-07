/*
 * Copyright (c) 2020 David Allison <davidallisongithub@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.ankiweb.rsdroid.database;

import androidx.annotation.NonNull;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import net.ankiweb.rsdroid.BackendFactory;

/**
 * Implementation of {@link SupportSQLiteOpenHelper.Factory} using the Anki Desktop backend
 */
public class RustVNextSQLiteOpenHelperFactory implements SupportSQLiteOpenHelper.Factory {
    private final BackendFactory backendFactory;

    public RustVNextSQLiteOpenHelperFactory(BackendFactory backendFactory) {
        this.backendFactory = backendFactory;
    }

    @NonNull
    @Override
    public SupportSQLiteOpenHelper create(@NonNull SupportSQLiteOpenHelper.Configuration configuration) {
        return new RustVNextSupportSQLiteOpenHelper(configuration, backendFactory);
    }
}
