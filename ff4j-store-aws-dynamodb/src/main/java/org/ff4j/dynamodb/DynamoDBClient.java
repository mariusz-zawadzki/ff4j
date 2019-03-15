package org.ff4j.dynamodb;

/*
 * #%L
 * ff4j-store-aws-dynamodb
 * %%
 * Copyright (C) 2013 - 2016 FF4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.util.CollectionUtils;
import org.ff4j.core.Feature;
import org.ff4j.exception.FeatureNotFoundException;
import org.ff4j.exception.GroupNotFoundException;
import org.ff4j.exception.PropertyNotFoundException;
import org.ff4j.property.Property;
import org.ff4j.utils.Util;

import java.util.*;

import static org.ff4j.dynamodb.DynamoDBConstants.*;

/**
 * @author <a href="mailto:jeromevdl@gmail.com">Jerome VAN DER LINDEN</a>
 */
public abstract class DynamoDBClient<T> {

    private final AmazonDynamoDB amazonDynamoDB;
    protected final DynamoDB dynamoDB;
    protected final String tableName;
    protected String key;
    protected Table table;

    public DynamoDBClient(AmazonDynamoDB amazonDynamoDB, String tableName) {
        this.amazonDynamoDB = amazonDynamoDB;
        this.dynamoDB = new DynamoDB(amazonDynamoDB);
        this.tableName = tableName;
        this.table = dynamoDB.getTable(tableName);
    }

    protected abstract void createTable();
    protected abstract RuntimeException notFoundException(String id);
    protected abstract T get(String id);
    protected abstract void put(T t);
    protected abstract Map<String, T> getAll();

    public void deleteItem(String id) {
        table.deleteItem(new KeyAttribute(key, id));
    }

    public Item getItem(String id) {
        Util.assertHasLength(id);

        Item item = table.getItem(new GetItemSpec().withPrimaryKey(new PrimaryKey(key, id)));
        if (item == null) {
            throw notFoundException(id);
        }
        return item;
    }

    public boolean tableExists() {
        try {
            amazonDynamoDB.describeTable(tableName);
            table = dynamoDB.getTable(tableName);
        } catch (ResourceNotFoundException e) {
            return false;
        }
        return true;
    }


    public void deleteTable() {
        table.delete();
        try {
            table.waitForDelete();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
