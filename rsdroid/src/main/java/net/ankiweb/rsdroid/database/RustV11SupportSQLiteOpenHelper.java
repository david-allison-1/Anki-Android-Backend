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
import androidx.annotation.Nullable;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import net.ankiweb.rsdroid.BackendFactory;
import net.ankiweb.rsdroid.BackendUtils;
import net.ankiweb.rsdroid.BackendV1;

import timber.log.Timber;

public class RustV11SupportSQLiteOpenHelper extends RustSupportSQLiteOpenHelper {

    public RustV11SupportSQLiteOpenHelper(@NonNull Configuration configuration, BackendFactory backendFactory) {
        super(configuration, backendFactory);
    }

    public RustV11SupportSQLiteOpenHelper(@NonNull BackendV1 backend) {
        super(backend);
    }

    @Override
    protected SupportSQLiteDatabase createRustSupportSQLiteDatabase(@SuppressWarnings("SameParameterValue") boolean readOnly) {
        Timber.d("createRustSupportSQLiteDatabase");
        if (configuration != null) {
            BackendV1 backend = backendFactory.getBackend();
            BackendUtils.openAnkiDroidCollection(backend, configuration.name);
            return new RustSupportSQLiteDatabase(backend, readOnly);
        } else {
            return new RustSupportSQLiteDatabase(backend, readOnly);
        }
    }
}
