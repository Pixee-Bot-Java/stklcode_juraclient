/*
 * Copyright 2016-2018 Stefan Kalscheuer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.stklcode.pubtrans.ura.model;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit test for the Trip metamodel.
 *
 * @author Stefan Kalscheuer
 */
public class TripTest {
    @Test
    public void basicConstructorTest() {
        Trip trip = new Trip("sid",
                "name",
                "indicator",
                1,
                2.345,
                6.789,
                123,
                "lineid",
                "linename",
                0,
                "destination name",
                "destination text",
                "vehicle",
                "id",
                123456789123456789L);
        assertThat(trip.getStop().getId(), is("sid"));
        assertThat(trip.getStop().getName(), is("name"));
        assertThat(trip.getStop().getIndicator(), is("indicator"));
        assertThat(trip.getStop().getState(), is(1));
        assertThat(trip.getStop().getLatitude(), is(2.345));
        assertThat(trip.getStop().getLongitude(), is(6.789));
        assertThat(trip.getVisitID(), is(123));
        assertThat(trip.getLineID(), is("lineid"));
        assertThat(trip.getLineName(), is("linename"));
        assertThat(trip.getDirectionID(), is(0));
        assertThat(trip.getDestinationName(), is("destination name"));
        assertThat(trip.getDestinationText(), is("destination text"));
        assertThat(trip.getVehicleID(), is("vehicle"));
        assertThat(trip.getId(), is("id"));
        assertThat(trip.getEstimatedTime(), is(123456789123456789L));
    }

    @Test
    public void listConstructorTest() {
        /* Create valid raw data list */
        List<Object> raw = new ArrayList<>();
        raw.add(1);
        raw.add("stopName");
        raw.add("stopId");
        raw.add("stopIndicator");
        raw.add(9);
        raw.add(8.765);
        raw.add(43.21);
        raw.add(123);
        raw.add("lineid");
        raw.add("linename");
        raw.add(0);
        raw.add("destination name");
        raw.add("destination text");
        raw.add("vehicle");
        raw.add(9876543210L);
        raw.add(123456789123456789L);

        try {
            Trip trip = new Trip(raw);
            assertThat(trip.getStop().getId(), is("stopId"));
            assertThat(trip.getStop().getName(), is("stopName"));
            assertThat(trip.getStop().getIndicator(), is("stopIndicator"));
            assertThat(trip.getStop().getState(), is(9));
            assertThat(trip.getStop().getLatitude(), is(8.765));
            assertThat(trip.getStop().getLongitude(), is(43.21));
            assertThat(trip.getVisitID(), is(123));
            assertThat(trip.getLineID(), is("lineid"));
            assertThat(trip.getLineName(), is("linename"));
            assertThat(trip.getDirectionID(), is(0));
            assertThat(trip.getDestinationName(), is("destination name"));
            assertThat(trip.getDestinationText(), is("destination text"));
            assertThat(trip.getVehicleID(), is("vehicle"));
            assertThat(trip.getId(), is("9876543210"));
            assertThat(trip.getEstimatedTime(), is(123456789123456789L));
        } catch (IOException e) {
            fail("Creation of Trip from valid list failed: " + e.getMessage());
        }

        /* Test with V2 style list */
        raw.set(14, "id");
        try {
            Trip trip = new Trip(raw, "2.0");
            assertThat(trip.getId(), is("id"));
        } catch (IOException e) {
            fail("Creation of Trip from valid list failed: " + e.getMessage());
        }
        raw.set(14, 9876543210L);

        /* Excess elements should be ignored */
        raw.add("foo");
        try {
            Trip trip = new Trip(raw);
            assertThat(trip, is(notNullValue()));
            raw.remove(16);
        } catch (IOException e) {
            fail("Creation of Trip from valid list failed: " + e.getMessage());
        }

        /* Test exceptions on invalid data */
        List<Object> invalid = new ArrayList<>(raw);
        invalid.remove(7);
        invalid.add(7, "123");
        try {
            new Trip(invalid);
            fail("Creation of Trip with invalid visitID field successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(8);
        invalid.add(8, 25);
        try {
            new Trip(invalid);
            fail("Creation of Trip with invalid lineID field successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(9);
        invalid.add(9, 234L);
        try {
            new Trip(invalid);
            fail("Creation of Trip with invalid line name field successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(10);
        invalid.add(10, "1");
        try {
            new Trip(invalid);
            fail("Creation of Trip with invalid directionID field successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(11);
        invalid.add(11, 987);
        try {
            new Trip(invalid);
            fail("Creation of Trip with invalid destinationName field successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(12);
        invalid.add(12, 456.78);
        try {
            new Trip(invalid);
            fail("Creation of Trip with invalid destinationText field successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(13);
        invalid.add(13, 'x');
        try {
            new Trip(invalid);
            fail("Creation of Trip with invalid vehicleID field successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(14);
        invalid.add(14, 1.2);
        try {
            new Trip(invalid);
            fail("Creation of Trip with invalid id field successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(15);
        invalid.add(15, 456);
        try {
            new Trip(invalid);
            fail("Creation of Trip with invalid estimatedTime field successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.remove(15);
        try {
            new Trip(invalid);
            fail("Creation of Trip with too short list successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }

        invalid = new ArrayList<>(raw);
        invalid.set(10, 3);
        try {
            new Trip(invalid);
            fail("Creation of Trip with direction ID 3 successfull");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        }
    }
}
