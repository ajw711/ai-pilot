package natsclient

import (
	"log"

	"github.com/nats-io/nats.go"
)

type Client struct {
	conn *nats.Conn
}

// 메소드가 아니라 함수임
func New(url string) (*Client, error) {
	nc, err := nats.Connect(url)
	if err != nil {
		return nil, err
	}

	log.Printf("[nats] connected to %s", url)
	return &Client{
		conn: nc,
	}, nil
}

// 함수가 아니라 메소드임
func (c *Client) Subscribe(subject string, handler func(msg *nats.Msg)) error {
	_, err := c.conn.Subscribe(subject, handler)
	if err != nil {
		return err
	}

	log.Printf("[nats] subscribed to subject=%s", subject)
	return nil
}

func (c *Client) Close() {
	if c.conn != nil && !c.conn.IsClosed() {
		log.Println("[nats] closing connection")
		c.conn.Close()
	}
}
