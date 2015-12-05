package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by Menno on 4-12-2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.MATCH_DAY,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.MATCH_ID
    };

    static final int COL_HOME = 0;
    static final int COL_AWAY = 1;
    static final int COL_HOME_GOALS = 2;
    static final int COL_AWAY_GOALS = 3;
    static final int COL_MATCHTIME = 6;
    static final int COL_MATCHID = 7;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                Uri uri = DatabaseContract.scores_table.buildScoreWithDate();
                String formatString = getString(R.string.dateformat_ymd);
                SimpleDateFormat format = new SimpleDateFormat(R.string.dateformat_ymd);
                String todayDate = format.format(new Date());

                data = getContentResolver().query(uri,
                        SCORE_COLUMNS,
                        null,
                        new String[] { todayDate },
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_soccer_item);

                String homeTeamName = data.getString(COL_HOME);
                String awayTeamName = data.getString(COL_AWAY);
                String matchTime = data.getString(COL_MATCHTIME);
                String score = Utilies.getTeamScore(DetailWidgetRemoteViewsService.this,
                        data.getInt(COL_HOME_GOALS), data.getInt(COL_AWAY_GOALS));

                views.setTextViewText(R.id.widget_home_name, homeTeamName);
                views.setTextViewText(R.id.widget_away_name, awayTeamName);
                views.setTextViewText(R.id.widget_match_time, matchTime);
                views.setTextViewText(R.id.widget_score, score);
                views.setImageViewResource(R.id.widget_home_crest, Utilies.getTeamCrestByTeamName(homeTeamName));
                views.setImageViewResource(R.id.widget_away_crest, Utilies.getTeamCrestByTeamName(awayTeamName));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, R.id.widget_home_crest, homeTeamName);
                    setRemoteContentDescription(views, R.id.widget_away_crest, awayTeamName);
                    setRemoteContentDescription(views, R.id.widget_home_name, homeTeamName);
                    setRemoteContentDescription(views, R.id.widget_away_name, awayTeamName);
                    setRemoteContentDescription(views, R.id.widget_match_time, matchTime);
                    setRemoteContentDescription(views, R.id.widget_score, score);
                }

                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, int id, String description) {
                views.setContentDescription(id, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_soccer_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(COL_MATCHID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
