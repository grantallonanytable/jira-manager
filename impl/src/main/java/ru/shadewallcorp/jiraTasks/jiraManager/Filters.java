package ru.shadewallcorp.jiraTasks.jiraManager;

import org.taymyr.lagom.metrics.MetricsFilter;
import play.http.DefaultHttpFilters;

import javax.inject.Inject;

/**
 * Фильтр с метриками.
 *
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
public class Filters extends DefaultHttpFilters {

    @Inject
    public Filters(MetricsFilter filter) {
        super(filter);
    }
}
