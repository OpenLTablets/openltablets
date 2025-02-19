import React, { useEffect, useState } from 'react'
import { Button, Form, Input, Row, Table } from 'antd'
import type { CheckboxValueType } from 'antd/es/checkbox/Group'
import { AlignType } from 'rc-table/lib/interface'
import { stringify } from 'querystring'
import CreateGroupDataSource from './CreateGroupDataSource'
import { apiCall } from '../../services'

interface EditGroupProps {
  group: {
    id: number;
    oldName: string
    name: string,
    description: string;
    roles: string[];
    privileges: string[];

  };
  updateGroup: (updatedGroup: any) => void;
  onSave: () => void;
}

export const EditGroupModal: React.FC<EditGroupProps> = ({ group, updateGroup, onSave }) => {
    const [name, setName] = useState('')
    const [description, setDescription] = useState('')
    const [selectedGroupPrivileges, setSelectedGroupPrivileges] = useState<string[]>([])
    const [privileges, setPrivileges] = useState<CheckboxValueType[]>(selectedGroupPrivileges)

  interface DataType {
    key: React.Key;
    privilege: string;
    administrators: string;
    analysts: string;
    deployers: string;
    developers: string;
    testers: string;
    viewers: string;
    [key: string]: string | React.Key;
  }

  useEffect(() => {
      setName(group.name)
      setDescription(group.description)
      setPrivileges(group.privileges)
      setSelectedGroupPrivileges(group.privileges)
  }, [group])

  const handleNameInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      setName(e.target.value)
  }

  const handleDescriptionInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      setDescription(e.target.value)
  }

  const handleSave = async () => {
      const updatedGroup = {
          ...group,
          oldName: '',
          name,
          description,
          privileges: privileges.map((privilege) => privilege.toString()),
      }
      const encodedBody = stringify(updatedGroup)

      try {
          const headers = new Headers()
          headers.append('Content-Type', 'application/x-www-form-urlencoded')
          headers.append('Accept', 'application/json')

          const response = await apiCall('/admin/management/groups', {
              method: 'POST',
              headers,
              body: encodedBody,
          })
          if (response.ok) {
              const responseData = await response.text()
              const groupData = responseData ? JSON.parse(responseData) : {}

              setName('')
              setDescription(groupData.description)
              setPrivileges(groupData.privileges)
              setSelectedGroupPrivileges(groupData.privileges)
              updateGroup(updatedGroup)
              onSave()
          } else {
              console.error('Error updating group:', response.statusText)
          }
      } catch (error) {
          console.error('Error updating group:', error)
      }
  }

  const selectPrivileges = (privilege: string) => {
      const selectedRows = CreateGroupDataSource.filter((row) => row[privilege] === '✓')
      const privileges = selectedRows.map((row) => row.key) as CheckboxValueType[]
      setPrivileges(privileges)
  }

  const rowSelection = {
      onChange: (selectedRowKeys: React.Key[], selectedRows: DataType[]) => {
          const privileges = selectedRows.map((row) => row.key) as CheckboxValueType[]
          setPrivileges(privileges)
      },
  }

  const columns = [
      {
          title: 'Privilege',
          dataIndex: 'privilege',
          key: 'privilege',
      },
      {
          title: (
              <div>
                  <Button
                      onClick={() => selectPrivileges('administrators')}
                      style={{ width: 95 }}
                      type="text"
                  >
                      Administrators
                  </Button>
              </div>
          ),
          dataIndex: 'administrators',
          key: 'administrators',
          align: 'center' as AlignType,
      },
      {
          title: (
              <div>
                  <Button onClick={() => selectPrivileges('analysts')} style={{ width: 70 }} type="text">
                      Analysts
                  </Button>
              </div>
          ),
          dataIndex: 'analysts',
          key: 'analysts',
          align: 'center' as AlignType,
      },
      {
          title: (
              <div>
                  <Button onClick={() => selectPrivileges('deployers')} style={{ width: 70 }} type="text">
                      Deployers
                  </Button>
              </div>
          ),
          dataIndex: 'deployers',
          key: 'deployers',
          align: 'center' as AlignType,
      },
      {
          title: (
              <div>
                  <Button onClick={() => selectPrivileges('developers')} style={{ width: 70 }} type="text">
                      Developers
                  </Button>
              </div>
          ),
          dataIndex: 'developers',
          key: 'developers',
          align: 'center' as AlignType,
      },
      {
          title: (
              <div>
                  <Button onClick={() => selectPrivileges('testers')} style={{ width: 70 }} type="text">
                      Testers
                  </Button>
              </div>
          ),
          dataIndex: 'testers',
          key: 'testers',
          align: 'center' as AlignType,
      },
      {
          title: (
              <div>
                  <Button onClick={() => selectPrivileges('viewers')} style={{ width: 70 }} type="text">
                      Viewers
                  </Button>
              </div>
          ),
          dataIndex: 'viewers',
          key: 'viewers',
          align: 'center' as AlignType,
      },
  ]

  return (
      <div>
          <Form layout="vertical">
              <Form.Item>
                  <b>Account</b>
              </Form.Item>
              <Form.Item label="Name">
                  <Input id="name" onChange={handleNameInputChange} value={name} />
              </Form.Item>
              <Form.Item label="Description">
                  <Input id="description" onChange={handleDescriptionInputChange} value={description} />
              </Form.Item>
              <Form.Item>
                  <b>Group</b>
              </Form.Item>
              <Form.Item className="group-create-form_last-form-item">
                  <Form.Item>
                      <Table
                          columns={columns}
                          dataSource={CreateGroupDataSource}
                          pagination={false}
                          rowSelection={rowSelection}
                          scroll={{ x: 'max-content' }}
                      />
                  </Form.Item>
              </Form.Item>
              <Form.Item>
                  <Row style={{ float: 'right' }}>
                      <Button
                          key="submit"
                          onClick={handleSave}
                          style={{ color: 'green', borderColor: 'green' }}
                      >
                          Save
                      </Button>
                  </Row>
              </Form.Item>
          </Form>
      </div>
  )
}
