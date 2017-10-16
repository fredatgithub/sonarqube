/*
 * SonarQube
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.plugins.edition;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.utils.HttpDownloader;
import org.sonar.server.platform.ServerFileSystem;
import org.sonar.updatecenter.common.Release;
import org.sonar.updatecenter.common.UpdateCenter;
import org.sonar.updatecenter.common.Version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EditionPluginDownloaderTest {
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private ServerFileSystem fs = mock(ServerFileSystem.class);
  private HttpDownloader httpDownloader = mock(HttpDownloader.class);
  private UpdateCenter updateCenter = mock(UpdateCenter.class);

  private File downloadDir;
  private EditionPluginDownloader downloader;

  @Before
  public void setUp() throws IOException {
    downloadDir = temp.newFolder();
    when(fs.getEditionDownloadedPluginsDir()).thenReturn(downloadDir);
    downloader = new EditionPluginDownloader(httpDownloader, fs);
  }

  @Test
  public void download_plugin_to_tmp_and_rename_it() throws IOException, URISyntaxException {
    String url = "http://host/plugin.jar";
    Release release = createRelease("pluginKey", "1.0", url);

    // mock file downloaded
    File tmp = new File(downloadDir, "plugin.jar.tmp");
    tmp.createNewFile();

    when(updateCenter.findInstallablePlugins("pluginKey", Version.create(""))).thenReturn(Collections.singletonList(release));

    downloader.installEdition(Collections.singleton("pluginKey"), updateCenter);

    verify(httpDownloader).download(new URI(url), tmp);
    assertThat(tmp).doesNotExist();
    assertThat(new File(downloadDir, "plugin.jar")).isFile();
  }

  private static Release createRelease(String key, String version, String url) {
    Release release = mock(Release.class);
    when(release.getKey()).thenReturn(key);
    when(release.getVersion()).thenReturn(Version.create(version));
    when(release.getDownloadUrl()).thenReturn(url);
    return release;
  }
}
