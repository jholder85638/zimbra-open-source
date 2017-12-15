/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite CSharp Client
 * Copyright (C) 2011, 2012, 2013, 2014, 2015, 2016 Synacor, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
#include "common.h"
#include "Exchange.h"

// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
// MAPITableIterator
// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
MAPITableIterator::MAPITableIterator(): 
    m_pTable(NULL), 
    m_pParentFolder(NULL), 
    m_pRows(NULL),
    m_currRow(0), 
    m_batchSize(200), 
    m_rowsVisited(0), 
    m_totalRows(0), 
    m_session(NULL) 
{
    LOGFN_VERBOSE;
}

MAPITableIterator::~MAPITableIterator()
{
    LOGFN_VERBOSE;
    if (m_pRows != NULL)
        FreeProws(m_pRows);
}

void MAPITableIterator::InitMAPITableIterator(const wstring& sObjID, LPMAPITABLE pTable, LPMAPIFOLDER pFolder, MAPISession &session, ULONG ulItemTypeMask)
{
    m_sObjectID = sObjID;

    LOGFN_VERBOSE;
    UNREFERENCED_PARAMETER(ulItemTypeMask);
    HRESULT hr = S_OK;

    m_session = &session;

    if (m_pParentFolder != NULL)
    {
        UlRelease(m_pParentFolder);
        m_pParentFolder = NULL;
    }

    if (m_pRows != NULL)
        FreeProws(m_pRows);

    m_pParentFolder = pFolder;
    m_pTable = pTable;

    hr = m_pTable->SetColumns(GetTableProps(), 0);
    if (FAILED(hr))
        throw GenericException(hr, L"MAPITableIterator::Initialize():SetColumns Failed.",ERR_SET_RESTRICTION, __LINE__, __FILE__);
    
    if (GetSortOrder() != NULL)
    {
        if (FAILED(hr = m_pTable->SortTable(GetSortOrder(), 0)))
            throw GenericException(hr, L"MAPITableIterator::Initialize():SortTable Failed.",ERR_SET_RESTRICTION, __LINE__, __FILE__);
    }

    if (FAILED(hr = m_pTable->GetRowCount(0, &m_totalRows)))
        throw GenericException(hr, L"MAPITableIterator::Initialize():GetRowCount Failed.",ERR_SET_RESTRICTION, __LINE__, __FILE__);

    // m_batchsize typically 200
    if (FAILED(hr = m_pTable->QueryRows(m_batchSize, 0, &m_pRows)))
        throw GenericException(hr, L"MAPITableIterator::Initialize():QueryRows Failed.",ERR_SET_RESTRICTION, __LINE__, __FILE__);
}

SRow *MAPITableIterator::GetNext()
{
    LOGFN_VERBOSE;
    HRESULT hr = S_OK;

    if (0 == m_totalRows)
        return NULL;

    if (m_currRow >= m_pRows->cRows)
    {
        if (m_rowsVisited >= m_totalRows)
            return NULL;

        FreeProws(m_pRows);
        m_pRows = NULL;

        if (m_totalRows - m_rowsVisited < m_batchSize)
        {
            hr = m_pTable->QueryRows((m_totalRows - m_rowsVisited), 0, &m_pRows);
            if (m_pRows && (m_pRows->cRows < (m_totalRows - m_rowsVisited)))
            {
                // "**Warning**: %d Table Rows Requested, Got just %d, hr = %d"), _totalRows - _rowsVisited, _pRows->cRows, hr
                m_rowsVisited = m_totalRows - m_pRows->cRows;
            }
        }
        else
        {
            hr = m_pTable->QueryRows(m_batchSize, 0, &m_pRows);
            if (m_pRows && (m_pRows->cRows < m_batchSize))
            {
                // "**Warning**: %d Table Rows Requested, Got just %d, hr = %d"), _batchSize, _pRows->cRows, hr
                m_rowsVisited += m_batchSize - m_pRows->cRows;
            }
        }
        if (FAILED(hr))
            throw GenericException(hr, L"MAPITableIterator::GetNext():QueryRows Failed.",ERR_GET_NEXT, __LINE__, __FILE__);

        m_currRow = 0;
    }

    if (!m_pRows->cRows)
        return NULL;

    SRow *pRow = &(m_pRows->aRow[m_currRow]);

    m_currRow++;
    m_rowsVisited++;

    return pRow;
}
