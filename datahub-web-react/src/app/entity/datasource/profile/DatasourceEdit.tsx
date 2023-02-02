import { EditOutlined } from '@ant-design/icons';
import { Button } from 'antd';
import React, { useState } from 'react';
import { useGetDatasourceQuery } from '../../../../graphql/datasource.generated';
import { Datasource } from '../../../../types.generated';
import { IFormData } from '../service/DataSourceType';
import { sourceTypeList } from '../service/FormInitValue';
import AddDataSourceModal from './AddDataSourceModal';
import { useGetAuthenticatedUser } from '../../../useGetAuthenticatedUser';

export type Props = {
    datasource: Datasource;
};

export default function DatasourceEdit({ datasource: { urn } }: Props) {
    const [showEditModal, setShowEditModal] = useState(false);
    const [loading, updateLoading] = useState(false);
    const corpUserUrn = useGetAuthenticatedUser()?.corpUser?.urn;
    const res = useGetDatasourceQuery({
        variables: {
            urn,
        },
    });
    console.log('datasource edit res...', res);
    const dataSource = res?.data?.datasource;
    const owned = res?.data?.datasource?.owned;
    const typeName = dataSource?.primaryConn?.connection?.__typename;
    const selectedType = sourceTypeList.find((item) => {
        return typeName?.toLocaleLowerCase().includes(item.value.toLocaleLowerCase());
    });
    const conn = dataSource?.primaryConn;
    const gsbConn = dataSource?.gsbConn;
    const conns = [
        {
            id: 1,
            ...conn?.connection,
            dataCenter: conn?.dataCenter,
        },
    ];
    if (gsbConn) {
        conns.push({
            id: 2,
            ...gsbConn.connection,
            dataCenter: gsbConn?.dataCenter,
        });
    }
    const originData: IFormData = {
        sourceType: selectedType?.value || '',
        alias: dataSource?.alias || '',
        testQuerySql: dataSource?.testQuerySql || '',
        name: dataSource?.name || '',
        create: false,
        group: dataSource?.group?.urn || '',
        region: dataSource?.region || '',
        connections: conns,
    };

    const updateModalStatus = () => {
        updateLoading(true);
        const timer = setTimeout(() => {
            clearTimeout(timer);
            updateLoading(false);
            setShowEditModal(true);
        }, 1000);
    };

    const showEdit = () => {
        return corpUserUrn !== undefined && corpUserUrn !== null && owned;
    };

    return (
        <>
            {showEdit() && (
                <Button type="link" onClick={updateModalStatus} loading={loading}>
                    Edit
                    <EditOutlined />
                </Button>
            )}
            {showEditModal && (
                <AddDataSourceModal
                    originData={originData}
                    visible
                    title="Edit DataSource"
                    corpUserUrn={corpUserUrn}
                    onClose={() => {
                        setShowEditModal(false);
                    }}
                />
            )}
        </>
    );
}
