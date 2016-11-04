/**
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the 
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 * 
 * OrbisGIS is distributed under GPL 3 license.
 *
 * Copyright (C) 2007-2014 CNRS (IRSTV FR CNRS 2488)
 * Copyright (C) 2015-2016 CNRS (Lab-STICC UMR CNRS 6285)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.sif.components.filter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.orbisgis.commons.events.EventException;
import org.orbisgis.commons.events.Listener;
import org.orbisgis.commons.events.ListenerContainer;
import org.orbisgis.sif.common.ContainerItemProperties;
import org.orbisgis.sif.components.CustomButton;
import org.orbisgis.sif.icons.SifIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Filter GUI and functionality is a generic concept of OrbisGIS GUI.
 * This manager help to 
 *  - introduce easily a filtering system
 *  - show the same GUI for the same functionality over all frames
 * @param <FilterInterface> The filter interface implements methods generated by
 * filter factories, this is specific to the DataModel
 * @param <FilterSerialisation>  
 * 
 */

public class FilterFactoryManager<FilterInterface,FilterSerialisation extends ActiveFilter> {
    /**
     * Use this interface to register a listener
     */
    public interface FilterChangeListener extends Listener<FilterChangeEventData> {            
    }
    private static final I18n I18N = I18nFactory.getI18n(FilterFactoryManager.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterFactoryManager.class);
    private JPanel filterListPanel;/*!< This panel contain the set of filters */
        //List of active filters
    private Map<Component,FilterSerialisation> filterValues = Collections.synchronizedMap(new HashMap<Component,FilterSerialisation>());
    //List of filter factories
    private Map<String,FilterFactory<FilterInterface,FilterSerialisation>> filterFactories = Collections.synchronizedMap(new HashMap<String,FilterFactory<FilterInterface,FilterSerialisation>>());
    //Factory index, this retrieve the factory name from an integer in
    //all JComboBox filter factories
    private List<ContainerItemProperties> filterFactoriesComboLabels = new ArrayList<ContainerItemProperties>();
    //The factory shown when the user click on new factory button
    private String defaultFilterFactory="";
    
    //Listener on filter change
    private ListenerContainer<FilterChangeEventData> eventFilterChange = new ListenerContainer<FilterChangeEventData>();
    private ListenerContainer<FilterChangeEventData> eventFilterFactoryChange  = new ListenerContainer<FilterChangeEventData>();
    
    private boolean userCanRemoveFilter = true;
    /**
     * Add a filter factory
     * @param filterFactory The filter factory instance
     */
    public final void registerFilterFactory(FilterFactory<FilterInterface,FilterSerialisation> filterFactory) {
        //Add filter factory in the HashMap
        filterFactories.put(filterFactory.getFactoryId(), filterFactory);
        
        //Add filter factory label and id in a list (for all GUI ComboBox)
        filterFactoriesComboLabels.add(new ContainerItemProperties(filterFactory.getFactoryId(),filterFactory.getFilterLabel()));
        
        if(defaultFilterFactory.isEmpty()) {
            defaultFilterFactory = filterFactory.getFactoryId();
        }
        //TODO if some filters are already shown, refresh all factories combo box (Future Plugin-Filter ?)
    }
    
     /**
     * Remove all filters registered with the provided factory id
     * @param factoryId The factory id returned by DataSourceFilterFactory.getFactoryId
     */
    public void removeFilters(String factoryId) {
        //Collect all components to remove
        Stack<Component> filterPanelsToRemove = new Stack<Component>();
        for(Map.Entry<Component,FilterSerialisation> filter : filterValues.entrySet()) {
            if(filter.getValue().getFactoryId().equals(factoryId)) {
                //Found a filter panel registered by factoryId
                filterPanelsToRemove.add(filter.getKey());
            }
        }
        //Remove components
        while(!filterPanelsToRemove.isEmpty()) {
            onRemoveFilter(filterPanelsToRemove.pop());
        }
    }
    
       /**
     * Call by listeners when the user click on the Remove button 
     * or change the factory combobox value
     * @param filterPanel The filter panel instance
     */
    public void onRemoveFilter(Component filterPanel) {
        //Remove the filter value
        filterValues.remove(filterPanel);
        //Remove the filter panel from the GUI filter list panel
        filterListPanel.remove(filterPanel);
        filterListPanel.updateUI();
        //Update filters
        fireFilterFactoryChange();
    }
    /**
     * The default factory when the user click on add filter button
     * @param defaultFilterFactory The name of the factory
     */
    public void setDefaultFilterFactory(String defaultFilterFactory) {
        this.defaultFilterFactory = defaultFilterFactory;
    }
    
    /**
     * The user click on add filter button
     */
    public void onAddFilter() {
        //Add the default filter
        FilterFactory<FilterInterface,FilterSerialisation> factory = filterFactories.get(defaultFilterFactory);
        addFilter(factory.getDefaultFilterValue());
    }
    
    
       /**
     * Add the swing filter component to the filter gui
     * This method will add a remove filter button,
     * a filter factory JComboBox and the specified component
     * @param newFilterComponent Returned by a DataSourceFilterFactory
     * @warning onFilterChanged Must retrieve the FilterFactoriesComboBox !
     */
    private void addFilterComponent(Component newFilterComponent,FilterSerialisation activeFilter) {
        //the factory name
        JPanel filterPanel = new JPanel(new BorderLayout());
        if(userCanRemoveFilter) {
                //Create the remove button
                JButton removeButton = makeRemoveFilterButton();
                //Attach listener, will call the onRemoveFilter method
                //with the parent container as argument
                removeButton.addActionListener(
                        EventHandler.create(
                        ActionListener.class, this, "onRemoveFilter","source.parent")
                );
                //Add the button in the filter panel
                filterPanel.add(removeButton,BorderLayout.WEST);
        }
        //Create a layout to contain the factory and filter components
        JPanel factoryAndFilter = new JPanel(new BorderLayout());
        if(filterFactories.size()>1) {
                //Create the filter factory combobox
                factoryAndFilter.add(makeFilterFactoriesComboBox(activeFilter.getFactoryId()),BorderLayout.WEST);
        } else {
                factoryAndFilter.add(makeFileFactoryLabel(filterFactories.get(activeFilter.getFactoryId()).getFilterLabel()),BorderLayout.WEST);
        }
        if(newFilterComponent!=null) {
            //Add the factory component in the filter panel
            factoryAndFilter.add(newFilterComponent,BorderLayout.CENTER);
        }
        filterPanel.add(factoryAndFilter,BorderLayout.CENTER);
        //Add the component in the filter list contents
        filterValues.put(filterPanel, activeFilter);
        //Add the component in the filter list GUI
        filterListPanel.add(filterPanel);   
        //Refresh the GUI
        filterListPanel.updateUI();
    }

    /**
     * @see FilterFactoryManager#setUserCanRemoveFilter(boolean)
     * @return False if the next remove buttons will not be created
     */
    public boolean isUserCanRemoveFilter() {
        return userCanRemoveFilter;
    }

    /**
     * Hide/Show The remove button at the left of filters
     * This parameter is only active on new filters
     * @param userCanRemoveFilter 
     */
    public void setUserCanRemoveFilter(boolean userCanRemoveFilter) {
        this.userCanRemoveFilter = userCanRemoveFilter;
    }
    
    /**
     * Remove all filters
     * Do not fire events
     */
    public void clearFilters() {
        filterValues.clear();
        filterListPanel.removeAll();
        filterListPanel.updateUI();
    }
    
    /**
     * Get all filter values currently shown
     * @return 
     */
    public Collection<FilterSerialisation> getFilterValues() {
            return filterValues.values();
    }
    
    /**
     * Create a new filter in the UI filter list
     * @param activeFilter The filter value
     */
    public void addFilter(FilterSerialisation activeFilter) {
        if(filterFactories.containsKey(activeFilter.getFactoryId())) {
            //Retrieve the factory
            FilterFactory<FilterInterface,FilterSerialisation> filterFactory = filterFactories.get(activeFilter.getFactoryId());
            if(filterFactory==null) {
                    throw new IllegalArgumentException(I18N.tr("The provided filter factory name has not been registered"));
            }
            //If the filter value is modified reloadFilters must be called
            activeFilter.addPropertyChangeListener(ActiveFilter.PROP_CURRENTFILTERVALUE,
                    EventHandler.create(PropertyChangeListener.class, this,"onFilterChanged"));
            //Create the Swing component
            Component swingFiterField = filterFactory.makeFilterField(activeFilter);
            addFilterComponent(swingFiterField, activeFilter);
            //Update the filters
            fireFilterFactoryChange();
        }
    }
    
    /**
     * Use the listener manager to track change on filters
     * Your list control must be updated with a new set of filters,
     * through the getFilters method
     * @return The listener manager
     */
    public ListenerContainer<FilterChangeEventData> getEventFilterChange() {
        return eventFilterChange;
    }
    
    /**
     * Use the listener manager to track change on filters factories
     * Your list control must be updated with a new set of filters,
     * through the getFilters method
     * @return The listener manager
     */
    public ListenerContainer<FilterChangeEventData> getEventFilterFactoryChange() {
        return eventFilterFactoryChange;
    }
    /**
     * Replace all FactoryComboBox by Labels
     * Navigation through components is quite difficult and verbose.
     * A reference could be used instead of doing the kind of navigation
     */
    private void replaceFactoryComboBoxByLabels() {
        boolean uiChange=false;
        for (Component removeButtonFactoryFilter : filterListPanel.getComponents()) {
            if(removeButtonFactoryFilter instanceof JPanel) {
                Component factoryAndFilter = ((BorderLayout)((JPanel)removeButtonFactoryFilter).getLayout()).getLayoutComponent(BorderLayout.CENTER);
                if(factoryAndFilter!=null) {
                    Component factoryList = ((BorderLayout)((JPanel)factoryAndFilter).getLayout()).getLayoutComponent(BorderLayout.WEST);
                    if(!(factoryList instanceof JComboBox || factoryList instanceof JLabel)) {
                        //Could not find Filter Factory list
                        //You must update onFilterChanged according to the change on Filter Factory ComboBox panel layout
                        LOGGER.debug("Error: Could not find Filter Factory list");
                    } else {
                        if(factoryList instanceof JComboBox) {
                            String itemLabel = ((JComboBox)factoryList).getSelectedItem().toString();
                            //Remove the factory list
                            ((JPanel)factoryAndFilter).remove(factoryList);
                            //Place the Label
                            ((JPanel)factoryAndFilter).add(makeFileFactoryLabel(itemLabel), BorderLayout.WEST);
                            ((JPanel)factoryAndFilter).doLayout();
                            uiChange=true;
                        }
                    }                    
                }
            }
        }
        
        if(uiChange) {
            filterListPanel.updateUI();
        }
    }
    /**
     * The user Add/Change/Remove filter type (factory).
     * @return true if the event has been accepted by all listeners
     */
    private boolean fireFilterFactoryChange() {
        try {
            //Fire event
            eventFilterFactoryChange.callListeners(new FilterChangeEventData(this));
            return true;
        } catch (EventException ex) {
            //The event has been refused by a listener
            return false;
        }              
    }
    
    
    /**
     * Fire the filter change event
     * The List must update the content according to the filters
     * @return true if the event has been accepted by all listeners
     */
    private boolean fireFilterChange() {
        try {
            //Fire event
            eventFilterChange.callListeners(new FilterChangeEventData(this));
            return true;
        } catch (EventException ex) {
            //The event has been refused by a listener
            return false;
        }        
    }
    
    /**
     * The input of a filter has been edited by the user
     */
    public void onFilterChanged() {
        //The user change the content of the filter
        //Then the user accept the current factories
        //Replace all factories by labels to free spaces
        //Only if the user is able to remove factories
        if(userCanRemoveFilter) {
                replaceFactoryComboBoxByLabels();
        }        
        fireFilterChange();
    }
    /**
     * Regenerate all filters from filters components
     * @return All active filters
     */
    public List<FilterInterface> getFilters() {
        List<FilterInterface> generatedFilters = new ArrayList<FilterInterface>();
        //For each active filter
        for(FilterSerialisation activeFilter : filterValues.values()) {
            if(filterFactories.containsKey(activeFilter.getFactoryId())) {
                //Retrieve the factory
                FilterFactory<FilterInterface,FilterSerialisation> filterFactory = filterFactories.get(activeFilter.getFactoryId());
                //Ask the factory to build the filter with the current value
                FilterInterface generatedFilter = filterFactory.getFilter(activeFilter);
                generatedFilters.add(generatedFilter);     
            }
        }
        //Set the filters and update the list
        return generatedFilters;
    }
    /**
     * The user selected a filter factory
     * A new filter is shown with the selected filter factory
     * @param filterFactoryId The filter factory name
     */
    public void onChooseFilterFactory(String filterFactoryId) {
        //Add a new filter with an empty value
        FilterFactory<FilterInterface,FilterSerialisation> factory = filterFactories.get(filterFactoryId);
        addFilter(factory.getDefaultFilterValue());
    }
    
    private JLabel makeFileFactoryLabel(String selectedFactory) {
            return new JLabel(selectedFactory);
    }
    
    
    /**
     * Create a new filter factories combo box
     * @return A new instance of filterFactoriesComboBox
     */ 
    private JComboBox makeFilterFactoriesComboBox(String selectedFactory) {
        //Set a unique data model for all filterFactoriesCombo
        JComboBox filterFactoriesCombo = new JComboBox(this.filterFactoriesComboLabels.toArray());
        //Select the factory
        filterFactoriesCombo.setSelectedItem(new ContainerItemProperties(selectedFactory, ""));
        //Add a listener to remove the filter
        filterFactoriesCombo.addActionListener(
                EventHandler.create(ActionListener.class, this,
                "onRemoveFilter","source.parent.parent"));
        //Add a listener to add a new filter with the selected factory
        filterFactoriesCombo.addActionListener(
                EventHandler.create(ActionListener.class, this,
                "onChooseFilterFactory","source.selectedItem.getKey"));
        return filterFactoriesCombo;
    }
    /**
     * Build the remove filter button component
     * @return The button
     * @note listener are created in this function
     */
    private JButton makeRemoveFilterButton() {
        //Create a compact button
        JButton removeFilterButton = new CustomButton(SifIcon.getIcon("delete"));
        removeFilterButton.setToolTipText(I18N.tr("Delete this filter"));
        return removeFilterButton;
    }
    /**
     * Build the add filter button component
     * @return The button
     * @note listener are created in this function
     */
    private Component makeAddFilterButton() {
        //This JPanel set the button at the top
        JPanel buttonAlignement = new JPanel(new BorderLayout());
        //Create a compact button
        JButton addFilterButton = new CustomButton(SifIcon.getIcon("add_filter"));
        buttonAlignement.add(addFilterButton,BorderLayout.NORTH);
        //Toottip
        addFilterButton.setToolTipText(I18N.tr("Add a new filter"));
        //Apply action listener
        addFilterButton.addActionListener( 
                EventHandler.create(ActionListener.class, this, "onAddFilter")                
                );        
        return buttonAlignement;
    }
    
    /**
     * Create the filter panel
     * @param createAddButton The manager will insert a button that will call the method onAddFilter
     * @return The builded panel
     */
    public JPanel makeFilterPanel(boolean createAddButton) {
        //This panel contain the button panel and the filter list panel
        JPanel buttonAndFilterList = new JPanel(new BorderLayout());
        if(createAddButton) {
            //Add the toggle button
            buttonAndFilterList.add(makeAddFilterButton(), BorderLayout.LINE_START);
        }
        //GridLayout with 1 column (vertical stack) and n(0) rows
        filterListPanel = new JPanel(new GridLayout(0,1));
        //Filter List must take all horizontal space
        //CENTER will expand the content to take all avaible place
        buttonAndFilterList.add(filterListPanel, BorderLayout.CENTER);
        //Add the AddFilter button and Filter list in the main filter panel
        return buttonAndFilterList;
    }
}
