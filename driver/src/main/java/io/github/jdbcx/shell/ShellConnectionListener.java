/*
 * Copyright 2022-2023, Zhichun Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.jdbcx.shell;

import java.sql.SQLException;
import java.util.Properties;

import io.github.jdbcx.CommandLine;
import io.github.jdbcx.ConnectionListener;
import io.github.jdbcx.Constants;
import io.github.jdbcx.Option;
import io.github.jdbcx.SqlExceptionUtils;

final class ShellConnectionListener implements ConnectionListener {
    static final Option OPTION_PATH = CommandLine.OPTION_CLI_PATH.update()
            .defaultValue(Constants.IS_WINDOWS ? "cmd /c" : "sh -c").build();
    static final Option OPTION_TIMEOUT = CommandLine.OPTION_CLI_TIMEOUT.update().defaultValue("30000").build();
    static final Option OPTION_TEST_ARGS = CommandLine.OPTION_CLI_TEST_ARGS.update().defaultValue("echo").build();

    final CommandLine shell;

    ShellConnectionListener(Properties config) {
        OPTION_TIMEOUT.setValue(config);
        OPTION_TEST_ARGS.setValue(config);

        shell = new CommandLine(OPTION_PATH.getValue(config), config);
    }

    @Override
    public String onQuery(String query) throws SQLException {
        try {
            return shell.execute(query);
        } catch (Exception e) {
            if (shell.ignoreError()) {
                return query;
            } else if (shell.warnOnError()) {
                throw SqlExceptionUtils.clientWarning(e);
            } else {
                throw SqlExceptionUtils.clientError(e);
            }
        }
    }
}
