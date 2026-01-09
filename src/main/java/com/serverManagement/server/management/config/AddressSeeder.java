package com.serverManagement.server.management.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.serverManagement.server.management.dao.rma.depot.SavedAddressRepository;
import com.serverManagement.server.management.entity.rma.depot.SavedAddressEntity;

import java.util.List;
import java.util.ArrayList;

@Component
public class AddressSeeder implements CommandLineRunner {

        @Autowired
        private SavedAddressRepository repository;

        @Override
        public void run(String... args) throws Exception {
                // Define list of initial addresses
                List<SavedAddressEntity> targets = new ArrayList<>();

                targets.add(create("MOTOROLA SOLUTIONS INDIA PRIVATE LIMITED",
                                "9TH FLOOR, MFAR MANYATA TECH PARK, GREENHEART PHASE-IV, NAGAWARA, URBAN, BANGALORE -560045 KARNATAKA, INDIA",
                                "29AAACM9343D1ZG"));

                targets.add(create("MOTOROLA SOLUTIONS INDIA PRIVATE LIMITED",
                                "C/o Communications Test Design India Pvt. Ltd., No.48/1, 2nd Main Road,, Peenya Industrial Area,, Bengaluru (Bangalore) Urban, Karnataka, 560058",
                                "29AAACM9343D1ZG"));

                targets.add(create("MOTOROLA SOLUTIONS INDIA PRIVATE LIMITED",
                                "BUILDING NO. 8A, 05TH FLOOR, DLF CYBER CITY, PHASE II, GURGAON 122002, HARYANA, INDIA",
                                "06AAACM9343D1ZO"));

                targets.add(create("MOTOROLA SOLUTIONS INDIA PRIVATE LIMITED",
                                "C/O DHL SUPPLY CHAIN INDIA PVT LTD. RECTANGLE NO. 5, KILLA NO. 6/2, 7, 5, 14 AND 15/1, VILLAGE BERHAMPUR, SECTOR 72, TEHSIL & DISTT. GURGAON, PIN-122001",
                                "06AAACM9343D1ZO"));

                targets.add(create("MOTOROLA SOLUTIONS INDIA PRIVATE LIMITED",
                                "C/O FLYITZ GLOBAL FORWARDERS LLP KHASRA NO. 15/21/2, NEAR TELEPHONE EXCHANGE, SAMALKA, NEW DELHI - 110037",
                                "07AAACM9343D1ZM"));

                targets.add(create("MOTOROLA SOLUTIONS INDIA PRIVATE LIMITED",
                                "SUITE#18-13, INFINITY BUSINESS CENTRE, INFINITY BENCHMARK, 18TH FLOOR, PLOT-G1, BLOCK- EP & GP, SECTOR- V, SALT LAKE ELECTRONIC COMPLEX, KOLKATA- 700091, WEST BENGAL, INDIA",
                                "19AAACM9343D2ZG"));

                // Insert individually if not exists (inefficient for bulk, but fine for 6 items
                // at startup)
                int added = 0;
                List<SavedAddressEntity> allExisting = repository.findAll();
                for (SavedAddressEntity target : targets) {
                        boolean exists = allExisting.stream().anyMatch(e -> isSame(e.getName(), target.getName())
                                        && isSame(e.getAddress(), target.getAddress()));

                        if (!exists) {
                                repository.save(target);
                                added++;
                        }
                }

                if (added > 0) {
                        System.out.println("Seeded " + added + " new Saved Addresses.");
                }
        }

        private SavedAddressEntity create(String name, String address, String gst) {
                SavedAddressEntity e = new SavedAddressEntity();
                e.setName(name);
                e.setAddress(address);
                e.setGstIn(gst);
                return e;
        }

        private boolean isSame(String s1, String s2) {
                if (s1 == null)
                        s1 = "";
                if (s2 == null)
                        s2 = "";
                return s1.trim().equalsIgnoreCase(s2.trim());
        }
}
