/* 
 Copyright (c) 2013 LDBC
 Linked Data Benchmark Council (http://www.ldbcouncil.org)
 
 This file is part of ldbc_snb_datagen.
 
 ldbc_snb_datagen is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 ldbc_snb_datagen is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with ldbc_snb_datagen.  If not, see <http://www.gnu.org/licenses/>.
 
 Copyright (C) 2011 OpenLink Software <bdsmt@openlinksw.com>
 All Rights Reserved.
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation;  only Version 2 of the License dated
 June 1991.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.*/
package ldbc.snb.datagen.serializer.snb.csv.dynamicserializer.person;

import com.google.common.collect.ImmutableList;
import ldbc.snb.datagen.DatagenMode;
import ldbc.snb.datagen.DatagenParams;
import ldbc.snb.datagen.dictionary.Dictionaries;
import ldbc.snb.datagen.entities.dynamic.person.Person;
import ldbc.snb.datagen.entities.dynamic.relations.Knows;
import ldbc.snb.datagen.entities.dynamic.relations.StudyAt;
import ldbc.snb.datagen.entities.dynamic.relations.WorkAt;
import ldbc.snb.datagen.util.DateUtils;
import ldbc.snb.datagen.hadoop.writer.HdfsCsvWriter;
import ldbc.snb.datagen.serializer.DynamicPersonSerializer;
import ldbc.snb.datagen.serializer.snb.csv.CsvSerializer;
import ldbc.snb.datagen.serializer.snb.csv.FileName;

import java.util.Iterator;
import java.util.List;

import static ldbc.snb.datagen.serializer.snb.csv.FileName.*;

public class CsvMergeForeignDynamicPersonSerializer extends DynamicPersonSerializer<HdfsCsvWriter> implements CsvSerializer {

    @Override
    public List<FileName> getFileNames() {
        return ImmutableList.of(PERSON, PERSON_SPEAKS_LANGUAGE, PERSON_EMAIL_EMAILADDRESS, PERSON_HASINTEREST_TAG,
                PERSON_WORKAT_ORGANISATION, PERSON_STUDYAT_ORGANISATION, PERSON_KNOWS_PERSON);
    }

    @Override
    public void writeFileHeaders() {
        List<String> dates = (DatagenParams.getDatagenMode() == DatagenMode.RAW_DATA) ?
                ImmutableList.of("creationDate", "deletionDate") :
                ImmutableList.of("creationDate");

        writers.get(PERSON)                     .writeHeader(dates, ImmutableList.of("id", "firstName", "lastName", "gender", "birthday",  "locationIP", "browserUsed", "place"));
        writers.get(PERSON_SPEAKS_LANGUAGE)     .writeHeader(dates, ImmutableList.of("Person.id", "language"));
        writers.get(PERSON_EMAIL_EMAILADDRESS)  .writeHeader(dates, ImmutableList.of("Person.id", "email"));
        writers.get(PERSON_HASINTEREST_TAG)     .writeHeader(dates, ImmutableList.of("Person.id", "Tag.id"));
        writers.get(PERSON_STUDYAT_ORGANISATION).writeHeader(dates, ImmutableList.of("Person.id", "Organisation.id", "classYear"));
        writers.get(PERSON_WORKAT_ORGANISATION) .writeHeader(dates, ImmutableList.of("Person.id", "Organisation.id", "workFrom"));
        writers.get(PERSON_KNOWS_PERSON)        .writeHeader(dates, ImmutableList.of("Person.id", "Person.id"));
    }

    @Override
    protected void serialize(final Person person) {
        List<String> dates = (DatagenParams.getDatagenMode() == DatagenMode.RAW_DATA) ?
                ImmutableList.of(Dictionaries.dates.formatDateTime(person.getCreationDate()), Dictionaries.dates.formatDateTime(person.getDeletionDate())) :
                ImmutableList.of(Dictionaries.dates.formatDateTime(person.getCreationDate()));

        //creationDate, [deletionDate,] id, firstName, lastName, gender, birthday, locationIP, browserUsed, place
        writers.get(PERSON).writeEntry(dates, ImmutableList.of(
            Long.toString(person.getAccountId()),
            person.getFirstName(),
            person.getLastName(),
            getGender(person.getGender()),
            Dictionaries.dates.formatDate(person.getBirthday()),
            person.getIpAddress().toString(),
            Dictionaries.browsers.getName(person.getBrowserId()),
            Integer.toString(person.getCityId())
        ));

        List<Integer> languages = person.getLanguages();
        for (int i = 0; i < languages.size(); i++) {
            //creationDate, [deletionDate,] Person.id, language
            writers.get(PERSON_SPEAKS_LANGUAGE).writeEntry(dates, ImmutableList.of(
                Long.toString(person.getAccountId()),
                Dictionaries.languages.getLanguageName(languages.get(i))
            ));
        }

        Iterator<String> emails = person.getEmails().iterator();
        while (emails.hasNext()) {
            //creationDate, [deletionDate,] Person.id, email
            writers.get(PERSON_EMAIL_EMAILADDRESS).writeEntry(dates, ImmutableList.of(
                Long.toString(person.getAccountId()),
                emails.next()
            ));
        }

        Iterator<Integer> interests = person.getInterests().iterator();
        while (interests.hasNext()) {
            //creationDate, [deletionDate,] Person.id, Tag.id
            writers.get(PERSON_HASINTEREST_TAG).writeEntry(dates, ImmutableList.of(
                Long.toString(person.getAccountId()),
                Integer.toString(interests.next())
            ));
        }
    }

    @Override
    protected void serialize(final StudyAt studyAt, final Person person) {
        List<String> dates = (DatagenParams.getDatagenMode() == DatagenMode.RAW_DATA) ?
                ImmutableList.of(Dictionaries.dates.formatDateTime(person.getCreationDate()), Dictionaries.dates.formatDateTime(person.getDeletionDate())) :
                ImmutableList.of(Dictionaries.dates.formatDateTime(person.getCreationDate()));

        //creationDate, [deletionDate,] Person.id, Organisation.id, classYear
        writers.get(PERSON_STUDYAT_ORGANISATION).writeEntry(dates, ImmutableList.of(
            Long.toString(studyAt.person),
            Long.toString(studyAt.university),
            DateUtils.formatYear(studyAt.year)
        ));
    }

    @Override
    protected void serialize(final WorkAt workAt, final Person person) {
        List<String> dates = (DatagenParams.getDatagenMode() == DatagenMode.RAW_DATA) ?
                ImmutableList.of(Dictionaries.dates.formatDateTime(person.getCreationDate()), Dictionaries.dates.formatDateTime(person.getDeletionDate())) :
                ImmutableList.of(Dictionaries.dates.formatDateTime(person.getCreationDate()));

        //creationDate, [deletionDate,] Person.id, Organisation.id, workFrom
        writers.get(PERSON_WORKAT_ORGANISATION).writeEntry(dates, ImmutableList.of(
            Long.toString(workAt.person),
            Long.toString(workAt.company),
            DateUtils.formatYear(workAt.year)
        ));
    }

    @Override
    protected void serialize(final Person person, Knows knows) {
        List<String> dates = (DatagenParams.getDatagenMode() == DatagenMode.RAW_DATA) ?
                ImmutableList.of(Dictionaries.dates.formatDateTime(person.getCreationDate()), Dictionaries.dates.formatDateTime(person.getDeletionDate())) :
                ImmutableList.of(Dictionaries.dates.formatDateTime(person.getCreationDate()));

        //creationDate, [deletionDate,] Person.id, Person.id
        writers.get(PERSON_KNOWS_PERSON).writeEntry(dates, ImmutableList.of(
            Long.toString(person.getAccountId()),
            Long.toString(knows.to().getAccountId())
        ));
    }

}