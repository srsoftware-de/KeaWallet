package de.keawe.keawallet.objects.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.Vector;

import de.keawe.keawallet.R;
import de.keawe.keawallet.TransactionList;
import de.keawe.keawallet.objects.Globals;

public class Category {

    private static final String TABLE_NAME = "categories";
    private static final String KEY = "id";
    private static final String CATEGORY = "category";
    private static final String PARENT = "parent";
    public static final String TABLE_CREATION = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PARENT+" LONG, "+CATEGORY + " VARCHAR(255))";
    private static HashMap<Long,Category> catList = new HashMap<>();
    private static LinearLayout.LayoutParams btnLayout = null; // will be created on first call of buttonLayout()
    private static LinearLayout.LayoutParams marginLayout = null; // will be created on first call of marginLayout()
    private final String definition;
    private long parent_id = 0;
    private long id = 0;
    private RelativeLayout layout = null;

    public Category(String def, long parent_id) {
        definition = def;
        this.parent_id = parent_id;
    }

    public static Category load(long id) {
        Category cat = catList.get(id);
        if (cat != null) return cat;

        SQLiteDatabase db = Globals.readableDatabase();
        Cursor cursor=db.query(TABLE_NAME,null,KEY+" = ?",new String[]{""+id},null,null,null);
        if (cursor.moveToNext()){
            String definition = cursor.getString(cursor.getColumnIndex(CATEGORY));
            long parent = cursor.getLong(cursor.getColumnIndex(PARENT));
            cat = new Category(definition,parent);
            cat.id = id;
            catList.put(id,cat);
        }
        db.close();
        return cat;
    }

    public static Vector<Category> loadRoots() {
        return loadByParent(0);
    }

    public static Vector<Category> loadByParent(long parent){

        Vector<Category> cats = new Vector<>();
        SQLiteDatabase db = Globals.readableDatabase();
        String selection = PARENT+" = "+parent;
        Cursor cursor=db.query(TABLE_NAME,null,selection,null,null,null,CATEGORY+" ASC");
        while (cursor.moveToNext()){
            long id = cursor.getLong(cursor.getColumnIndex(KEY));
            Category cat = catList.get(id);
            if (cat == null){
                String definition = cursor.getString(cursor.getColumnIndex(CATEGORY));
                parent = cursor.getLong(cursor.getColumnIndex(PARENT));
                System.out.println("id: "+id+", parent: "+parent+", def: "+definition);
                cat = new Category(definition,parent);
                cat.id = id;
                catList.put(id,cat);
            }
            cats.add(cat);
        }
        db.close();
        return cats;
    }

    @Override
    public String toString() {
        return "Category(id: "+id+", parent: "+parent_id+", def: "+definition+")";
    }

    public static String preset() {
        StringBuffer result = new StringBuffer("INSERT OR IGNORE INTO "+TABLE_NAME+"("+KEY+", "+CATEGORY+", "+PARENT+") VALUES ");

        int[][] values = {
                { 1,R.string.category_insurance, 0},
                { 2,R.string.category_life_insurance, 1},
                { 3,R.string.category_health_insurance, 1},
                { 4,R.string.category_mobility, 0},
                { 5,R.string.category_mobility_car,4},
                { 6,R.string.category_mobility_public,4},
                { 7,R.string.category_consumption,0},
                { 8,R.string.category_food,7},
                { 9,R.string.category_leisure,0},
                {10,R.string.category_income,0},
                {11,R.string.category_ventures,0},
                {12,R.string.category_fees,0},
                {13,R.string.category_accomodation,0}
        };

        for (int[] entry:values) result.append("("+entry[0]+", '"+Globals.string(entry[1])+"', "+entry[2]+") ");

        return result.toString().replace(") (","), (");
    }

    public RelativeLayout getView(final TransactionList transactionList) {
        layout = (RelativeLayout) transactionList.getLayoutInflater().inflate(R.layout.category_list_entry,null);

        Button assignCategoryButtin = (Button) layout.findViewById(R.id.assign_category_button);
        assignCategoryButtin.setText(definition);
        assignCategoryButtin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transactionList.loadTransactioList(Category.this);
            }
        });

        final LinearLayout childList = (LinearLayout) layout.findViewById(R.id.category_child_list);
        final ImageButton toggleButton = (ImageButton) layout.findViewById(R.id.toggle_category_button);

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (childList.getVisibility() == View.VISIBLE){
                    childList.setVisibility(View.GONE);
                    toggleButton.setImageResource(R.drawable.expand);
                } else {
                    childList.setVisibility(View.VISIBLE);
                    toggleButton.setImageResource(R.drawable.collapse);
                }
            }
        });


        for (Category child:children()) childList.addView(child.getView(transactionList));

        toggleButton.setVisibility(childList.getChildCount()>0?View.VISIBLE:View.GONE);

        ImageButton addSubcategoryButton = (ImageButton) layout.findViewById(R.id.add_sub_category_button);
        addSubcategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Adding subcategory not implemented,yet");
            }
        });

        return layout;
    }





    private Vector<Category> children() {
        return Category.loadByParent(id);
    }

    public long getId() {
        return id;
    }

    public void displayTransaction(Context context, Transaction t) {
        if (layout != null){
            LinearLayout childList = (LinearLayout) layout.findViewById(R.id.category_child_list);
            childList.addView(t.getView(context));

            ImageButton toggleButton = (ImageButton) layout.findViewById(R.id.toggle_category_button);
            toggleButton.setVisibility(View.VISIBLE);
        }
    }
}