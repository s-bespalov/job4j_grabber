package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    public static void main(String[] args) {
        try (var connection = Rabbit.getConnection()) {
            var scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            var jobData = new JobDataMap();
            jobData.put("connection", connection);
            var job = newJob(Rabbit.class)
                    .setJobData(jobData)
                    .build();
            var times = simpleSchedule()
                    .withIntervalInSeconds(Rabbit.getInterval())
                    .repeatForever();
            var trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (SchedulerException | SQLException | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        private static final Properties CONFIG = getProperties();

        private static Properties getProperties() {
            Properties config = new Properties();
            try (var is = Rabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
                config.load(is);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return config;
        }

        private static void saveCreatedTimeSQL(Connection connection) throws SQLException {
            try (var statement = connection.createStatement()) {
                statement.execute("INSERT INTO rabbit(created_date) VALUES (CURRENT_TIMESTAMP);");
            }
        }

        public static int getInterval() {
            return Integer.parseInt(CONFIG.getProperty("rabbit.interval"));
        }

        public static Connection getConnection() throws SQLException, ClassNotFoundException {
            Class.forName(CONFIG.getProperty("driver"));
            return DriverManager.getConnection(
                    CONFIG.getProperty("url"),
                    CONFIG.getProperty("username"),
                    CONFIG.getProperty("password")
            );
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            var connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try {
                saveCreatedTimeSQL(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}