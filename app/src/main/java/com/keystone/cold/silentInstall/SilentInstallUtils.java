/*
 * Copyright (c) 2021 X1-BTC-PSBT-Firmware
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file LICENSE.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.x1-btc-psbt-firmware.cold.silentInstall;

import android.content.Context;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.x1-btc-psbt-firmware.cold.silentInstall.constants.InstallConstants;
import com.x1-btc-psbt-firmware.cold.silentInstall.executor.MainExecutor;
import com.x1-btc-psbt-firmware.cold.silentInstall.listener.InstallResultListener;
import com.x1-btc-psbt-firmware.cold.silentInstall.observer.PackageInstallObserver;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SilentInstallUtils {

    private static final MainExecutor mainExecutor = new MainExecutor();

    private SilentInstallUtils() {

    }


    public static void runReplaceInstall(Context context,
                                         final String path,
                                         final InstallResultListener installResultListener) {
        int flag = InstallConstants.INSTALL_REPLACE_EXISTING | InstallConstants.INSTALL_DONT_KILL_APP;
        runInstall(context, path, flag, installResultListener);
    }

    private static void runInstall(final Context context,
                                   final String path,
                                   final int flag,
                                   final InstallResultListener installResultListener) {
        final PackageInstallObserver packageInstallObserver =
                new PackageInstallObserver(mainExecutor, installResultListener);
        new Thread(() -> {
            try {
                Uri uri = Uri.fromFile(new File(path));
                install(context, uri, packageInstallObserver, flag, null);
            } catch (Exception e) {
                packageInstallObserver.failure(e.getMessage());
            }
        }).start();
    }

    private static void install(Context context,
                                Uri packageURI,
                                IPackageInstallObserver observer,
                                int flags,
                                String installerPackageName) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {

        PackageManager packageManager = context.getPackageManager();
        Class<?> packageManagerClass = packageManager.getClass();
        Method installPackageMethod = packageManagerClass.getDeclaredMethod("installPackage",
                Uri.class, IPackageInstallObserver.class, int.class, String.class);
        if (installPackageMethod != null) {
            installPackageMethod.setAccessible(true);
            installPackageMethod.invoke(packageManager, packageURI, observer, flags, installerPackageName);
        } else {
            ((PackageInstallObserver) observer).failure("PackageManager installPackage is null");
        }
    }

}
