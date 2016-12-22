package com.example.rishabh.you4to;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawer;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("You4To");
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.nav_open, R.string.nav_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.drawer_navigation);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        Log.e("Intent", intent.toString());
        if(intent.hasExtra(Intent.EXTRA_TEXT)){
            navigationView.getMenu().getItem(1).setChecked(true);
            Bundle args = new Bundle();
            args.putString(Intent.EXTRA_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT));
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = new PlayerFragment();
            fragment.setArguments(args);
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }
        else {
            onNavigationItemSelected(navigationView.getMenu().getItem(0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            drawer.openDrawer(GravityCompat.START);
        }

        return super.onOptionsItemSelected(item);
    }

    /** Navigation Listener */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        //selectItem(id);
        if (id == R.id.add_link) {
            Fragment fragment = new AddLinkFragment();
            Bundle args = new Bundle();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        } else {
            Bundle args = new Bundle();
            args.putString(Intent.EXTRA_TEXT, "");
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = new PlayerFragment();
            fragment.setArguments(args);
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }
        item.setChecked(true);
        drawer.closeDrawers();
        return true;
    }

    public NavigationView getNavigationView(){
        return navigationView;
    }
}
