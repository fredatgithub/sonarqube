/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.batch.rules;

import org.sonar.batch.rule.QProfile;
import org.sonar.core.UtcDateUtils;
import org.sonar.core.qualityprofile.db.QualityProfileDao;
import org.sonar.core.qualityprofile.db.QualityProfileDto;

/**
 * @since 4.4
 */
public class DefaultQProfileReferential implements QProfilesReferential {

  private QualityProfileDao qualityProfileDao;

  public DefaultQProfileReferential(QualityProfileDao qualityProfileDao) {
    this.qualityProfileDao = qualityProfileDao;
  }

  @Override
  public QProfile get(String language, String name) {
    QualityProfileDto dto = qualityProfileDao.getByNameAndLanguage(name, language);
    if (dto == null) {
      return null;
    }
    QProfile profile = new QProfile();
    profile.setKey(dto.getKey());
    profile.setName(dto.getName());
    profile.setLanguage(dto.getLanguage());
    profile.setRulesUpdatedAt(UtcDateUtils.parseDateTime(dto.getRulesUpdatedAt()));
    return profile;
  }

}
