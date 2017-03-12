/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudera.sqoop.hive;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cloudera.sqoop.manager.ConnManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.sqoop.util.SqlTypeMap;

import com.cloudera.sqoop.SqoopOptions;
import com.cloudera.sqoop.tool.ImportTool;
import com.cloudera.sqoop.testutil.HsqldbTestServer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;

import java.sql.Types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Test Hive DDL statement generation.
 */
public class TestTableDefWriter {

  public static final Log LOG = LogFactory.getLog(
      TestTableDefWriter.class.getName());

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  // Test getHiveOctalCharCode and expect an IllegalArgumentException.
  private void expectExceptionInCharCode(int charCode) {
    thrown.expect(IllegalArgumentException.class);
    TableDefWriter.getHiveOctalCharCode(charCode);
  }

  @Test
  public void testHiveOctalCharCode() {
    assertEquals("\\000", TableDefWriter.getHiveOctalCharCode(0));
    assertEquals("\\001", TableDefWriter.getHiveOctalCharCode(1));
    assertEquals("\\012", TableDefWriter.getHiveOctalCharCode((int) '\n'));
    assertEquals("\\177", TableDefWriter.getHiveOctalCharCode(0177));

    expectExceptionInCharCode(4096);
    expectExceptionInCharCode(0200);
    expectExceptionInCharCode(254);
  }

  @Test
  public void testDifferentTableNames() throws Exception {
    Configuration conf = new Configuration();
    SqoopOptions options = new SqoopOptions();
    TableDefWriter writer = new TableDefWriter(options, null,
        "inputTable", "outputTable", conf, false);

    Map<String, Integer> colTypes = new SqlTypeMap<String, Integer>();
    writer.setColumnTypes(colTypes);

    String createTable = writer.getCreateTableStmt();
    String loadData = writer.getLoadDataStmt();

    LOG.debug("Create table stmt: " + createTable);
    LOG.debug("Load data stmt: " + loadData);

    // Assert that the statements generated have the form we expect.
    assertTrue(createTable.indexOf(
        "CREATE TABLE IF NOT EXISTS `outputTable`") != -1);
    assertTrue(loadData.indexOf("INTO TABLE `outputTable`") != -1);
    assertTrue(loadData.indexOf("/inputTable'") != -1);
  }

  @Test
  public void testDifferentTargetDirs() throws Exception {
    String targetDir = "targetDir";
    String inputTable = "inputTable";
    String outputTable = "outputTable";

    Configuration conf = new Configuration();
    SqoopOptions options = new SqoopOptions();
    // Specify a different target dir from input table name
    options.setTargetDir(targetDir);
    TableDefWriter writer = new TableDefWriter(options, null,
        inputTable, outputTable, conf, false);

    Map<String, Integer> colTypes = new SqlTypeMap<String, Integer>();
    writer.setColumnTypes(colTypes);

    String createTable = writer.getCreateTableStmt();
    String loadData = writer.getLoadDataStmt();

    LOG.debug("Create table stmt: " + createTable);
    LOG.debug("Load data stmt: " + loadData);

    // Assert that the statements generated have the form we expect.
    assertTrue(createTable.indexOf(
        "CREATE TABLE IF NOT EXISTS `" + outputTable + "`") != -1);
    assertTrue(loadData.indexOf("INTO TABLE `" + outputTable + "`") != -1);
    assertTrue(loadData.indexOf("/" + targetDir + "'") != -1);
  }

  @Test
  public void testPartitions() throws Exception {
    String[] args = {
        "--hive-partition-key", "ds",
        "--hive-partition-value", "20110413",
    };
    Configuration conf = new Configuration();
    SqoopOptions options =
      new ImportTool().parseArguments(args, null, null, false);
    TableDefWriter writer = new TableDefWriter(options,
        null, "inputTable", "outputTable", conf, false);

    Map<String, Integer> colTypes = new SqlTypeMap<String, Integer>();
    writer.setColumnTypes(colTypes);

    String createTable = writer.getCreateTableStmt();
    String loadData = writer.getLoadDataStmt();

    assertNotNull(createTable);
    assertNotNull(loadData);
    assertEquals("CREATE TABLE IF NOT EXISTS `outputTable` ( ) "
        + "PARTITIONED BY (ds STRING) "
        + "ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\054' "
        + "LINES TERMINATED BY '\\012' STORED AS TEXTFILE", createTable);
    assertTrue(loadData.endsWith(" PARTITION (ds='20110413')"));
  }

  @Test
  public void testLzoSplitting() throws Exception {
    String[] args = {
        "--compress",
        "--compression-codec", "lzop",
    };
    Configuration conf = new Configuration();
    SqoopOptions options =
      new ImportTool().parseArguments(args, null, null, false);
    TableDefWriter writer = new TableDefWriter(options,
        null, "inputTable", "outputTable", conf, false);

    Map<String, Integer> colTypes = new SqlTypeMap<String, Integer>();
    writer.setColumnTypes(colTypes);

    String createTable = writer.getCreateTableStmt();
    String loadData = writer.getLoadDataStmt();

    assertNotNull(createTable);
    assertNotNull(loadData);
    assertEquals("CREATE TABLE IF NOT EXISTS `outputTable` ( ) "
        + "ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\054' "
        + "LINES TERMINATED BY '\\012' STORED AS "
        + "INPUTFORMAT 'com.hadoop.mapred.DeprecatedLzoTextInputFormat' "
        + "OUTPUTFORMAT "
        + "'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'",
        createTable);
  }

  @Test
  public void testUserMapping() throws Exception {
    String[] args = {
        "--map-column-hive", "id=STRING,value=INTEGER",
    };
    Configuration conf = new Configuration();
    SqoopOptions options =
      new ImportTool().parseArguments(args, null, null, false);
    TableDefWriter writer = new TableDefWriter(options,
        null, HsqldbTestServer.getTableName(), "outputTable", conf, false);

    Map<String, Integer> colTypes = new SqlTypeMap<String, Integer>();
    colTypes.put("id", Types.INTEGER);
    colTypes.put("value", Types.VARCHAR);
    writer.setColumnTypes(colTypes);

    String createTable = writer.getCreateTableStmt();

    assertNotNull(createTable);

    assertTrue(createTable.contains("`id` STRING"));
    assertTrue(createTable.contains("`value` INTEGER"));

    assertFalse(createTable.contains("`id` INTEGER"));
    assertFalse(createTable.contains("`value` STRING"));
  }

  @Test
  public void testUserMappingFailWhenCantBeApplied() throws Exception {
    String[] args = {
        "--map-column-hive", "id=STRING,value=INTEGER",
    };
    Configuration conf = new Configuration();
    SqoopOptions options =
      new ImportTool().parseArguments(args, null, null, false);
    TableDefWriter writer = new TableDefWriter(options,
        null, HsqldbTestServer.getTableName(), "outputTable", conf, false);

    Map<String, Integer> colTypes = new SqlTypeMap<String, Integer>();
    colTypes.put("id", Types.INTEGER);
    writer.setColumnTypes(colTypes);

    thrown.expect(IllegalArgumentException.class);
    String createTable = writer.getCreateTableStmt();
  }

  @Test
  public void testHiveDatabase() throws Exception {
    String[] args = {
        "--hive-database", "db",
    };
    Configuration conf = new Configuration();
    SqoopOptions options =
      new ImportTool().parseArguments(args, null, null, false);
    TableDefWriter writer = new TableDefWriter(options,
        null, HsqldbTestServer.getTableName(), "outputTable", conf, false);

    Map<String, Integer> colTypes = new SqlTypeMap<String, Integer>();
    writer.setColumnTypes(colTypes);

    String createTable = writer.getCreateTableStmt();
    assertNotNull(createTable);
    assertTrue(createTable.contains("`db`.`outputTable`"));

    String loadStmt = writer.getLoadDataStmt();
    assertNotNull(loadStmt);
    assertTrue(createTable.contains("`db`.`outputTable`"));
  }

  @Test
  public void testGetCreateTableStmtDecimal() throws Exception {
    String[] args = {};
    Configuration conf = new Configuration();
    SqoopOptions options =
        new ImportTool().parseArguments(args, null, null, false);

    ConnManager connMgr = Mockito.mock(ConnManager.class);

    Map<String, List<Integer>> colInfo = new SqlTypeMap<String, List<Integer>>();
    List<Integer> info1 = new ArrayList<Integer>(3);
    List<Integer> info2 = new ArrayList<Integer>(3);
    List<String> columnNames = new ArrayList<String>();

    columnNames.add("decimal_column1");
    columnNames.add("decimal_column2");

    info1.add(Types.DECIMAL);
    info1.add(4);
    info1.add(2);

    info2.add(Types.DECIMAL);
    info2.add(44);
    info2.add(256);

    colInfo.put("decimal_column1", info1);
    colInfo.put("decimal_column2", info2);

    when(connMgr.getColumnNames("inputTable")).thenReturn(columnNames.toArray(new String[columnNames.size()]));
    when(connMgr.getColumnInfo("inputTable")).thenReturn(colInfo);
    when(connMgr.toHiveType("inputTable", "decimal_column1", 3)).thenReturn(
        HiveTypes.toHiveType(Types.DECIMAL)
    );
    when(connMgr.toHiveType("inputTable", "decimal_column2", 3)).thenReturn(
        HiveTypes.toHiveType(Types.DECIMAL)
    );

    TableDefWriter writer = new TableDefWriter(options,
        connMgr, "inputTable", "targetTable", conf, false);

    String createTable = writer.getCreateTableStmt();

    assertTrue(createTable.contains("`decimal_column1` DECIMAL(4, 2)"));
    assertTrue(createTable.contains("`decimal_column2` DECIMAL(38, 38)"));
  }
}
