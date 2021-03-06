package aabrasha.ua.streettranslator.view.activity;

import aabrasha.ua.streettranslator.R;
import aabrasha.ua.streettranslator.model.SortMethod;
import aabrasha.ua.streettranslator.service.StreetsService;
import aabrasha.ua.streettranslator.util.TextWatcherAdapter;
import aabrasha.ua.streettranslator.util.ViewUtils;
import aabrasha.ua.streettranslator.view.adapter.SearchPatternProvider;
import aabrasha.ua.streettranslator.view.fragment.ResultsFragment;
import aabrasha.ua.streettranslator.view.fragment.SearchFragment;
import aabrasha.ua.streettranslator.view.fragment.dialog.SelectSortMethodDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import static aabrasha.ua.streettranslator.util.StringUtils.EMPTY_STRING;
import static java.lang.String.format;

/**
 * @author Andrii Abramov on 8/27/16.
 */
public class SearchActivity extends AppCompatActivity {

    private static final String TAG = SearchActivity.class.getSimpleName();

    private EditText etSearch;

    private FragmentManager fragmentManager;
    private ResultsFragment resultsFragment;
    private SearchFragment searchFragment;
    private StreetsService streetsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        initFragments();
        initServices();

        findStreets();
    }

    private void initServices() {
        streetsService = StreetsService.getInstance();
    }

    private void initFragments() {
        fragmentManager = getSupportFragmentManager();
        resultsFragment = new ResultsFragment();
        searchFragment = new SearchFragment();

        initSearchFragment();
        initResultsFragment();
    }

    private void initSearchFragment() {
        searchFragment = (SearchFragment) initFragment(R.id.fragment_search, searchFragment);
    }

    private void initResultsFragment() {
        resultsFragment = (ResultsFragment) initFragment(R.id.fragment_results, resultsFragment);
        resultsFragment.setPatternProvider(new SearchPatternProvider() {
            @Override
            public String getPattern() {
                return etSearch.getText().toString();
            }
        });
    }

    private Fragment initFragment(int fragmentId, Fragment fragmentToUse) {
        Fragment destination = fragmentManager.findFragmentById(fragmentId);
        if (destination == null) {
            fragmentManager.beginTransaction()
                    .add(fragmentId, fragmentToUse)
                    .commit();
            return fragmentToUse;
        }
        return destination;
    }

    private void initViews() {
        etSearch = (EditText) findViewById(R.id.et_search);

        etSearch.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                findStreets();
            }
        });

        findViewById(R.id.btn_clear_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etSearch.setText(EMPTY_STRING);

                focusSearchField();
            }
        });
    }

    private void findStreets() {
        resultsFragment.refreshStreetsResults();
    }

    private void focusSearchField() {
        ViewUtils.focusWithKeyboard(etSearch);
    }

    @Override
    public void onBackPressed() {
        focusSearchField();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_populate_with_default_streets:
                confirmPopulateWithDefaultDialog();
                break;
            case R.id.menu_item_clear_streets_database:
                confirmClearDatabaseDialog();
                break;
            case R.id.menu_item_sort_method:
                showSortMethodDialog();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void showSortMethodDialog() {
        SelectSortMethodDialog dialog = SelectSortMethodDialog.newInstance(new SelectSortMethodDialog.OnSortMethodChangedHandler() {
            @Override
            public void onSortMethodChanged(SortMethod newSortMethod) {
                resultsFragment.setSortMethod(newSortMethod);
            }
        });
        dialog.show(getSupportFragmentManager(), TAG);
    }

    private void confirmPopulateWithDefaultDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.title_add_default_streets)
                .setMessage(R.string.hint_are_you_sure_to_populate_database_with_default_streets)
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        populateWithDefaultStreetList();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    private void confirmClearDatabaseDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.title_deleting_streets)
                .setMessage(R.string.hint_are_you_sure_to_delete_all_streets)
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        clearStreetsDatabase();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();

    }

    private void populateWithDefaultStreetList() {
        new AsyncPopulateWithDefaultTask().execute();
    }

    private void clearStreetsDatabase() {
        new AsyncClearDatabaseTask().execute();
    }

    private class AsyncPopulateWithDefaultTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            return streetsService.fillWithSampleData();
        }

        @Override
        protected void onPostExecute(Integer numOfAddedRows) {
            String report = getResources().getString(R.string.report_streets_added);
            Toast.makeText(SearchActivity.this, String.format(report, numOfAddedRows), Toast.LENGTH_SHORT).show();
            findStreets();
        }
    }

    private class AsyncClearDatabaseTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            return streetsService.cleanDatabase();
        }

        @Override
        protected void onPostExecute(Integer numOfDeletedRows) {
            String report = getResources().getString(R.string.report_streets_removed);
            Toast.makeText(SearchActivity.this, format(report, numOfDeletedRows), Toast.LENGTH_SHORT).show();
            findStreets();
        }
    }

}
